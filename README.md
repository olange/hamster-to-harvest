# Hamster to Harvest

A utility script written in [Clojure](http://clojure.org) to migrate
[Hamster](http://projecthamster.wordpress.com/about/) time tracking entries
to the [Harvest](https://www.getharvest.com) time tracking web service,
from an XML export to a CSV file -- which you can then import online.

## Status

Working executable released. I used it to migrate a dozen of Hamster projects
to Harvest. Defining the mapping requires hacking the sources (see below).

Look at the [develop](https://github.com/olange/hamster-to-harvest-csv/tree/develop) branch
for the latest state of the sources.

## Usage

```bash
$ ./hamster-to-harvest hamster.xml --output harvest.csv
                       [--append] [--filter:name PROJNAME]
                       [--config hamster-to-harvest.conf]
```

## Examples

All activities at once:

```bash
$ ./hamster-to-harvest hamster.xml --output harvest-bsa.csv
```

Incrementally, project by project:

```bash
$ ./hamster-to-harvest hamster.xml --filter:name PROJNAME1 --output harvest.csv
$ ./hamster-to-harvest hamster.xml --filter:name PROJNAME2 --output harvest.csv --append
```

## Migration process

1. Export the activites from Hamster in XML format.

2. Adjust the configuration to your needs:

    ```bash
    $ vi hamster-to-harvest.conf
    ```

3. Convert them to Harvest time tracking entries in CSV format (see usage below for more options):

    ```bash
    $ ./hamster-to-harvest hamster.xml --output harvest.csv
                                      [--append] [--filter:name PROJNAME]
    ```

4. Upload the resulting CSV file to your Harvest account; from the web interface:

   * _Company Settings_ › _Import Data into Harvest_ › _Import Timesheets From CSV_
   * select your `harvest.csv` file, and click _Upload and Import_

5. You'll shortly receive an e-mail from Harvest, with a link to the results of the import:

<img src="doc/images/harvest-import-confirm.png" height="175" />

## Caveat

The _Started at_, _Ended at_ and _Billed?_ fields of Harvest cannot be defined thru the CSV Import feature. So you'll loose the `start_time` and `end_time` fields of your Hamster activities.

Write to Harvest to ask them to include these valuable fields in the CSV importer; I did and they told they might consider adding them, if there was demand.

## Mapping

The mapping happens currently in the source code. Everything is handled within
the [mapping.clj](src/hamster_to_harvest/mapping.clj) script.

I believe you should be able to adjust the mapping to your requirements, even
if you do not know the [Clojure](http://clojure.org) language, but have experience
with another scripting language.

Hopefully you'll find sample idiomatic code within the script, which you can augment
and tweek. See the [Clojure Docs](https://clojuredocs.org) for a description of
Clojure built-in functions.

### Source XML format

Activities in Hamster are exported in the following XML structure:

```xml
<?xml version="1.0" ?>
<activities>
  <activity name="ZENwebdev"
            category="offert"
            tags="Publication"
            description="Actualisé page d'accueil selon demandes AM des 09.12 et 21.12.2010"
            duration_minutes="20"
            start_time="2011-01-03 00:45:00"
            end_time="2011-01-03 01:05:00" />

  <activity name="RZOhomepage"
            category="work"
            tags="Design graphique, facturé"
            description="Etude nouvelle mise en forme homepage"
            duration_minutes="120"
            start_time="2011-01-07 09:00:00"
            end_time="2011-01-07 11:00:00" />
</activities>
```

### Target CSV format

The Harvest CSV importer requires time entries in the following CSV format and
structure; the mapping should yield values for each of the following fields:

```csv
"Date","Client","Project","Task","Notes","Hours","First name","Last name"
2011-01-03,"Client ZEN","Site web","Actualisation du site","Actualisé page d'accueil selon demandes AM des 09.12 et 21.12.2010 [transcrit de Hamster]",0.3333333333333333,"Olivier","Lange"
2011-01-07,"Client RZO","Refonte homepage","Conception graphique","Etude nouvelle mise en forme homepage [transcrit de Hamster]",2,"Olivier","Lange"
…
```

## Compiling and assembling

If you're new to Clojure, here's how to get started hacking this project.
These instruction should be everything you need to adjust the mapping in
the sources.

Prerequisites:

* you'll need a [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.6+)
* and [Leiningen](http://leiningen.org/#install) installed on your computer.

### All at once

To create a console executable, in the base folder of the project
(works on Windows, Linux and Mac OSX):

```bash
$ lein bin
Compiling hamster-to-harvest.core
Compiling hamster-to-harvest.hamster
Compiling hamster-to-harvest.harvest
Compiling hamster-to-harvest.mapping
Created …/hamster-to-harvest-csv/target/uberjar+uberjar/hamster-to-harvest-0.2.0.jar
Created …/hamster-to-harvest-csv/target/uberjar/hamster-to-harvest-0.2.0-standalone.jar
Creating standalone executable: …/hamster-to-harvest-csv/target/base+system+user+dev/hamster-to-harvest
Copying binary to ./
```

This single command will:

1. download required dependencies;
2. compile the sources and package them in an executable [JAR](https://en.wikipedia.org/wiki/JAR_(file_format));
3. bundle this JAR and its dependencies in a self-contained executable UberJAR;
4. and wrap this executable UberJAR in a standalone console executable.

### In separate steps

To download all required dependencies (needed once only) and compile the sources:

```bash
$ lein deps
$ lein compile
```

To run the application from the command-line (which would also download the
dependencies and compile the sources, if this had not be done before):

```bash
$ lein run -- --help
$ lein run -- hamster.xml -o harvest.csv
```

To package the application as a self-contained JAR file (in the `target/` sub-folder):

```bash
$ lein uberjar
```

To hack from the REPL:

```clojure
$ lein repl
hamster-to-harvest.core=> (require '[hamster-to-harvest.core] :reload-all)
hamster-to-harvest.core=> (-main "hamster.xml" "-o" "harvest.csv")
```

Running the tests:

```clojure
$ lein test

lein test hamster-to-harvest.core-test
Converting Hamster activities from 'resources/hamster-sample.xml'
to Harvest time tracking entries into 'resources/harvest-sample.test.csv'

lein test hamster-to-harvest.mapping-test

Ran 6 tests containing 8 assertions.
0 failures, 0 errors.
```

## License

<a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png" /></a>
<br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Hamster to Harvest (CSV)</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="http://github.com/olange" property="cc:attributionName" rel="cc:attributionURL">Olivier Lange</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License</a>.
Permissions beyond the scope of this license may be available at <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/olange/hamster-to-harvest-csv/issues/new" rel="cc:morePermissions">github.com/hamster-to-harvest-csv/issues</a>.
