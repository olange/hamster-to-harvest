# Hamster to Harvest

A utility script written in Clojure to migrate [Hamster](http://projecthamster.wordpress.com/about/) time tracking entries to the [Harvest](https://www.getharvest.com) time tracking web service, from an XML export to a CSV file -- which you can then import online.

## Status

Alpha stage, working executable. Mapping and configuration requires hacking the sources.

Look at the [develop](https://github.com/olange/hamster-to-harvest-csv/tree/develop) branch
for the latest state of the sources.

## Usage

    $ ./hamster-to-harvest hamster.xml --output harvest.csv
                           [--append] [--filter:name PROJNAME]
                           [--config hamster-to-harvest.conf]

# Examples

All activities at once:

    $ ./hamster-to-harvest hamster.xml --output harvest-bsa.csv

Incrementally, project by project:

    $ ./hamster-to-harvest hamster.xml --filter:name PROJNAME1 --output harvest.csv
    $ ./hamster-to-harvest hamster.xml --filter:name PROJNAME2 --output harvest.csv --append

## Migration process

1. Export the activites from Hamster in XML format.

2. Adjust the configuration to your needs:

    ````
    $ cp resources/hamster-to-harvest.conf ./hamster-to-harvest.conf
    $ vi hamster-to-harvest.conf
    ````

3. Convert them to Harvest time tracking entries in CSV format (see usage below for more options):

    ````
    $ ./hamster-to-harvest hamster.xml --output harvest.csv
                                       --config hamster-to-harvest.conf
                                      [--append] [--filter:name PROJNAME]
    ````

4. Upload the resulting CSV file to your Harvest account; from the web interface:

   * _Company Settings_ › _Import Data into Harvest_ › _Import Timesheets From CSV_
   * select your `harvest.csv` file, and click _Upload and Import_

5. You'll shortly receive an e-mail from Harvest, with a link to the results of the import:

<img src="doc/images/harvest-import-confirm.png" height="175" />

## Caveat

The _Started at_, _Ended at_ and _Billed?_ fields of Harvest cannot be set thru the CSV Import feature. So you'll loose the `start_time` and `end_time` fields of your Hamster activities.

## Compiling and assembling

If you're new to Clojure, here's how to get started hacking this project.

Prerequisites:

* you'll need a [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.6+)
* and [Leiningen](http://leiningen.org/#install) installed on your computer.

To download all required dependencies (needed once only) and compile the sources:

    $ lein deps
    $ lein compile

To run the application from the command-line (which would also download the
dependencies and compile the sources, if this had not be done before):

    $ lein run -- --help
    $ lein run -- hamster.xml -o harvest.csv

To package the application as a self-contained JAR file (in the 'target/' sub-folder):

    $ lein uberjar

To create a cross-platform executable (in the base folder of the project):

    $ lein bin

To hack from the REPL:

    $ lein repl
    hamster-to-harvest.core=> (require '[hamster-to-harvest.core] :reload-all)
    hamster-to-harvest.core=> (-main "hamster.xml" "-o" "harvest.csv")

## License

<a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png" /></a>
<br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Hamster to Harvest (CSV)</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="http://github.com/olange" property="cc:attributionName" rel="cc:attributionURL">Olivier Lange</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License</a>.
Permissions beyond the scope of this license may be available at <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/olange/hamster-to-harvest-csv/issues/new" rel="cc:morePermissions">github.com/hamster-to-harvest-csv/issues</a>.
