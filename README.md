
# Batch Effects Interface

 * Rehan Akbani (owner)
 * John Weinstein
 * Bradley Broom
 * Tod Casasent (developer)

Batch Effects Interface is an HTTP GUI interface to a Batch Effects Interface (BEI) Docker Compose Stack. BEI includes images for a GUI component, an MBatch processing component, a Viewer and analysis component, and a component for downloading and converting GDC data for use with BEI.

### Batch Effects Interface Docker Quick Start

Download the docker-compose.yml file at the root of this repository. This file is setup for use on Linux.
(The context for the services has also been changed to allow running from this location.)

Make the following directories.

 - /BEI/OUTPUT
 - /BEI/PROPS
 - /BEI/UTIL
 - /BEI/WEBSITE
 - /BEI/MW_CACHE
 - /BEI/MW_ZIP

Then copy directory contents.

 1. Copy the contents of inst/ext/MW_CACHE into /BEI/MW_CACHE.
 2. Copy the contents of inst/ext/OUTPUT into /BEI/OUTPUT.
 3. Copy the contents of inst/ext/PROPS into /BEI/PROPS.
 4. Copy the contents of inst/ext/UTIL into /BEI/UTIL.
 5. Copy the contents of inst/ext/WEBSITE into /BEI/WEBSITE.

Permissions or ownership of the directories may need to be changed or matched to the Docker image user 2004.

In the directory with the docker-compose.yml file run:

    docker compose -p beihub -f docker-compose.yml up --no-build -d

You can stop it with:

    docker compose -p beihub -f docker-compose.yml down

To connect to the MBatch Omic Browser with:

	localhost:8080/BEI/BEI

**For educational and research purposes only.**

**Funding** 
This work was supported in part by U.S. National Cancer Institute (NCI) grant: Weinstein, Mills, Akbani. Batch effects in molecular profiling data on cancers: detection, quantification, interpretation, and correction, 5U24CA210949

