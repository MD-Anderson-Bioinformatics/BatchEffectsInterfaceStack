// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>
package edu.mda.bcb.sv.matrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cjacoby
 *
 * Description:
 * -----------------------------------------------------------------------------
 * A class to represent the row and column structure of a matrix data file.
 * Makes a first pass through the file and creates an index of the columns and
 * rows. A column index is the columns numerical position with respect to the
 * original column header order. A row index is the byte offset at which that
 * row begins in the file. Rows/Columns can then be sorted/filtered by the
 * row/column header. The write method then writes out the matrix, preserving
 * any sorting/filtering done with the instance.
 *
 * The column/row properties are ArrayLists of Header instance, which will
 * represent ordering and allow sorting. The columnSet/rowSet properties are
 * HashSets which represent the register of current entries and allow filtering.
 *
 * Notes:
 * -----------------------------------------------------------------------------
 * - Using the custom BufferedRandomAccessFile as a solution to getting a
 * persistent accurate byte-offset. Also gives performance improvement over
 * default RandomAccessFile. - The meat and potatoes of this class are in the
 * methods initRows, initColumns, and write. The other methods and data
 * structure manipulations are fairly straight forward.
 */
public final class Matrix
{

	// File Structure Properties
	private final String newline;                                               // The newline delimiter
	private final String delim;                                                 // The value delimiter
	private final boolean allowNonRectangle;                                    // Flag to allow/block varying lengths of row data

	// Data Properties
	final private String path;                                                  // Path to the input matrix file
	final private ArrayList<Header> columns;                                    // List of columns in their current order
	final private ArrayList<Header> rows;                                       // List of rows in their currrent order
	final private HashSet<String> columnSet;                                    // HashSet of column labels in the matrix
	final private HashSet<String> rowSet;                                       // HashSet of row labels in the matrix
	private String idHeader;                                                    // Column Header for the indices (row labels). Very first cell.
	private String standIn;                                                     // The stand-in value used for rows/cols added with addRow/addCol

	// Misc
	final private Pattern cellCountPattern;                                     // Pattern to count cells, ensuring rectangular matrix

	/**
	 * Private Constructor. Access through static Builder.
	 */
	private Matrix(
			String path,
			String newline,
			String delim,
			boolean allowNonRectangle,
			String standIn) throws IOException, Exception
	{
		// Builder Parameters
		this.path = path;
		this.newline = newline;
		this.delim = delim;
		this.allowNonRectangle = allowNonRectangle;
		this.standIn = standIn;
		// Construction
		this.cellCountPattern = Pattern.compile(this.delim);
		this.columns = new ArrayList<>();
		this.rows = new ArrayList<>();
		this.columnSet = new HashSet<>();
		this.rowSet = new HashSet<>();
		this.initColumns();
		this.initRows();
	}

	/**
	 * Pseudo public constructor. Instantiate Matrix from single path argument,
	 * assuming defaults for all other parameters. In all other use cases, use
	 * Builder.
	 */
	public static Matrix withDefualts(String path) throws IOException, Exception
	{
		Builder builder = new Builder(path);
		return new Matrix(path,
				builder.newline,
				builder.delim,
				builder.allowNonRectangle,
				builder.standIn);
	}

	/**
	 * Static Builder class to build a Matrix with optioned parameters.
	 */
	public static class Builder
	{

		// Defualt parameters for Matrix. Override with builder methods.
		private String path;
		private String newline = "\n";
		private String delim = "\t";
		private boolean allowNonRectangle = false;
		private String standIn = "Unknown";

		public Builder(String path)
		{
			this.path = path;
		}

		// public Builder withNewline(String newline) {
		//     this.newline = newline;
		//     return this;
		// }
		public Builder withDelimiter(String delim)
		{
			this.delim = delim;
			return this;
		}

		public Builder allowNonRectangle(boolean allow)
		{
			this.allowNonRectangle = allow;
			return this;
		}

		public Builder withStandIn(String standIn)
		{
			this.standIn = standIn;
			return this;
		}

		public Matrix build() throws IOException, Exception
		{
			Matrix m = new Matrix(
					this.path,
					this.newline,
					this.delim,
					this.allowNonRectangle,
					this.standIn);
			return m;
		}
	}

	/**
	 * Read the data file's first line, parse the headers, and store as Header
	 * instances in this.columns.
	 */
	public void initColumns() throws FileNotFoundException, IOException
	{
		try (BufferedReader br = new BufferedReader(new FileReader(new File(this.path))))
		{
			String firstLine = br.readLine();
			String[] toks = firstLine.split(this.delim);
			this.idHeader = toks[0];
			for (int i = 1; i < toks.length; i++)
			{
				this.columns.add(new Header(toks[i], i));
				this.columnSet.add(toks[i]);
			}
		}
	}

	/**
	 * Loop over the file, and create Header instances for each row. The Header
	 * instance contains the row label, and the byte offset for the beginning of
	 * the row.
	 */
	public void initRows() throws FileNotFoundException, IOException, Exception
	{
		try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(this.path, "r"))
		{
			long offset;
			int lineNum = 0;
			String line = braf.getNextLine();
			int headerCellCount = this.countCells(line);
			offset = braf.getFilePointer();
			lineNum += 1;
			while (true)
			{
				line = braf.getNextLine();
				lineNum += 1;
				if (line == null)
				{
					break;
				}
				line.replaceAll("\r", "").replaceAll("\n", "");
				if (!this.allowNonRectangle)
				{
					int rowCellCount = this.countCells(line);
					if (rowCellCount != headerCellCount)
					{
						throw new MatrixException("Row Size Violation at Line: " + lineNum
								+ ", where number of cells is: " + rowCellCount
								+ ", but number of headers is: " + headerCellCount
								+ ". ");
					}
				}
				String rowLabel = line.substring(0, line.indexOf(this.delim));
				this.rows.add(new Header(rowLabel, offset));
				this.rowSet.add(rowLabel);
				offset = braf.getFilePointer();
			}
		}
	}

	/**
	 * Return the Matrix instance path
	 */
	public String getPath()
	{
		return this.path;
	}

	/**
	 * Print the path, column length, and row length. Debugging purposes.
	 */
	public void printInfo()
	{
		System.out.println("Matrix Info:\n"
				+ "\t" + "path: " + this.path + ".\n"
				+ "\t" + "columns: " + this.columns.size() + ".\n"
				+ "\t" + "rows: " + this.rows.size() + ".\n");
	}

	public void sortColumns()
	{
		this.sort(this.columns);
	}

	public void sortRows()
	{
		this.sort(this.rows);
	}

	/**
	 * Sort an ArrayList, either columns or rows. TODO: Possibly allow
	 * sorting order as argument.
	 */
	public void sort(ArrayList<Header> headers)
	{
		Collections.sort(headers, Header.HEADER_SORTED_ORDER);
	}

	public void write(String outPath) throws FileNotFoundException, IOException
	{
		this.write(outPath, this.delim);
	}

	/**
	 * Write the matrix. Will write in sorted order if columns/rows were sorted.
	 * Iterates on the ArrayList row/columns instances, but checks HashSet
	 * rowSet/columnSet instances to see if an entry was filtered out.
	 */
	public void write(String outPath, String delimiter) throws FileNotFoundException, IOException
	{
		BufferedRandomAccessFile braf = new BufferedRandomAccessFile(this.path, "r");
		BufferedWriter brw = new BufferedWriter(new FileWriter(new File(outPath)));
		brw.write(this.idHeader);
		try
		{
			for (Header col : this.columns)
			{
				if (!this.columnSet.contains(col.label))
				{
					// This column was filtered out using removeColumn. Pass it.
					continue;
				}
				brw.write(delimiter + col.label);
			}
			brw.write(this.newline);
			for (Header row : this.rows)
			{
				if (!this.rowSet.contains(row.label))
				{
					// This row was filtered out using removeRow. Pass it.
					continue;
				}
				brw.write(row.label);
				if (row.index == null)
				{
					// This row was added after instantiation. Set all column values to standIn.
					for (int i = 0; i < this.columnSet.size(); i++)
					{
						brw.write(delimiter + this.standIn);
					}
				}
				else
				{
					braf.seek((long) row.index);
					String line = braf.getNextLine().replaceAll("\r", "").replaceAll("\n", "");
					String[] toks = line.split(delimiter);
					for (Header col : this.columns)
					{
						if (!this.columnSet.contains(col.label))
						{
							// This column was filtered out using removeColumn. Pass it.
							continue;
						}
						if (col.index == null)
						{
							// This column was added after instantiation. Set value to standIn.
							brw.write(delimiter + this.standIn);
						}
						else
						{
							brw.write(delimiter + toks[(int) col.index]);
						}
					}
				}
				brw.write(this.newline);
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			braf.close();
			brw.close();
		}
	}

	/**
	 * Get the columns.
	 */
	public ArrayList<Header> getColumns()
	{
		return this.columns;
	}

	/**
	 * Get the rows.
	 */
	public ArrayList<Header> getRows()
	{
		return this.rows;
	}

	/**
	 * Get the columnSet.
	 */
	public HashSet<String> getColumnSet()
	{
		return this.columnSet;
	}

	/**
	 * Get the rowSet.
	 */
	public HashSet<String> getRowSet()
	{
		return this.rowSet;
	}

	/**
	 * Check if this Matrix has a column by the label.
	 */
	public boolean hasColumn(String col)
	{
		return this.columnSet.contains(col);
	}

	/**
	 * Check if this Matrix has a row by the label.
	 */
	public boolean hasRow(String row)
	{
		return this.rowSet.contains(row);
	}

	/**
	 * Remove a column by the label
	 */
	public boolean removeColumn(String col)
	{
		return this.columnSet.remove(col);
	}

	/**
	 * Remove a column by the label
	 */
	public boolean removeRow(String row)
	{
		return this.rowSet.remove(row);
	}

	/**
	 * Add a column. All column values for the added column will be set to the
	 * Matrix's standIn instance property.
	 */
	public boolean addColumn(String name)
	{
		boolean b1 = this.columns.add(new Header(name, null));
		boolean b2 = this.columnSet.add(name);
		return b1 && b2;
	}

	/**
	 * Add a row. All row values for the added row will be set to the Matrix's
	 * standIn instance property.
	 */
	public boolean addRow(String name)
	{
		boolean b1 = this.rows.add(new Header(name, null));
		boolean b2 = this.rowSet.add(name);
		return b1 && b2;
	}

	/**
	 * Count the cells from a file line. Count by number of value delimiters.
	 *
	 * Line from a readLine() execution.
	 * Cell Count.
	 */
	private int countCells(String line)
	{
		int cellCount = 0;
		Matcher matchRowCells = this.cellCountPattern.matcher(line);
		while (matchRowCells.find())
		{
			cellCount++;
		}
		return cellCount;
	}

	/**
	 * Filter the Matrix m1 by the header values of Matrix m2. A.k.a. remove all
	 * headers in m1 that aren't also in m2. Specific to an axis, either row or
	 * column.
	 *
	 * matrix The matrix to be filtered.
	 * filterSet The filter set.
	 * axis The axis, 0 (row) or 1 (column)
	 */
	public static void filterMatrix(Matrix matrix, HashSet filterSet, int axis)
	{
		switch (axis)
		{
			case 0:
				ArrayList<Header> rows = matrix.getRows();
				for (int i = 0; i < rows.size(); i++)
				{
					if (!filterSet.contains(rows.get(i).label))
					{
						matrix.removeRow(rows.get(i).label);
					}
				}
				break;
			case 1:
				ArrayList<Header> cols = matrix.getColumns();
				for (int i = 0; i < cols.size(); i++)
				{
					if (!filterSet.contains(cols.get(i).label))
					{
						matrix.removeColumn(cols.get(i).label);
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Filter axis must be 0 (row-wise) or 1 (column-wise)");
		}
	}

	/**
	 * Write a current Matrix instance to the same path it was built form.
	 * Requires re-instantiating matrix upon completion.
	 */
	public static void replaceExisting(Matrix m) throws IOException
	{
		String path = m.getPath();
		File f = new File(path);
		String fileName = f.getName();
		String tempFileName = fileName;
		if (fileName.contains("."))
		{
			int extIndex = tempFileName.lastIndexOf(".");
			String ext = tempFileName.substring(extIndex, tempFileName.length());
			tempFileName = new StringBuilder(tempFileName.substring(0, extIndex) + "_temp" + ext).toString();
		}
		else
		{
			tempFileName += "_temp";
		}
		String tempPath = new File(f.getParent(), tempFileName).getAbsolutePath();
		m.write(tempPath);
		Files.copy(Paths.get(tempPath), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
		new File(tempPath).delete();
	}

}
