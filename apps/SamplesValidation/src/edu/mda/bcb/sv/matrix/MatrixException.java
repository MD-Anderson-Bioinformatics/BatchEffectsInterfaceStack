package edu.mda.bcb.matrix;

/**
 *
 * @author cjacoby
 */
public class MatrixException extends Exception {
    
    public MatrixException() {
        super();
    }
    
    public MatrixException(String message) {
        super(message);
    }
    
    public MatrixException(Throwable cause) {
        super(cause);
    }
    
    public MatrixException(String message, Throwable cause) {
        super(message, cause);
    }
  
}
