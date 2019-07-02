# Batch Effects Interface

 * Rehan Akbani (owner)
 * John Weinstein
 * Bradley Broom
 * Tod Casasent (developer)

Batch Effects Interface is an HTTP GUI interface to a Batch Effects Interface (BEI) Docker Compose Stack. BEI includes images for a GUI component, an MBatch processing component, a Viewer and analysis component, and a component for downloading and converting GDC data for use with BEI.

## Quick Start with Docker:

Clone the GitHub repository with a shallow clone, since you will not be checking anything back in. This clone call grabs the newest version from master.

>
>git clone --depth 1 https://github.com/MD-Anderson-Bioinformatics/BatchEffectsInterfaceStack.git
> 

Edit the file scripts/02_sed.bash as described in the documentation https://github.com/MD-Anderson-Bioinformatics/BatchEffectsInterfaceStack/blob/master/docs/BEI_01A_InstallExternalLinux.pdf

Create the Dockerfile using the edited scripts/02_sed.bash. Setup the directories you specified when editing the bash script.

With the resulting docker-compose.yml file, pull the images with:

>
>docker-compose -f docker-compose.yml pull


In the /BEI_EXT/build/bei-stack/ directory with the docker-compose.yml file, the containers (stack) are started using:

>
>docker-compose -p EXT -f docker-compose.yml up -d


The EXT may be varied if needed on your system to ensure unique ids for the stack.

The Docker Compose Stack can be stopped using:

>
>docker-compose -p EXT -f docker-compose.yml down
>
