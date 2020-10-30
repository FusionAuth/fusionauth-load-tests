## FusionAuth Load Tests ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)

## Examples Usages:

### Build

```
sb int
```

### Setup FusionAuth

You're on your own... 

### Run a load test

Register users, see `src/main/resources/User-Registrations.json` for test definition.

````
cd build/dist
./load-test.sh User-Registrations.json
````


Run login tests. See `src/main/resources/User-Logins.json` for test definition.

````
cd build/dist
./load-test.sh User-Logins.json
````

### Building with Savant

**Note:** This project uses the Savant build tool. To compile using Savant, follow these instructions:

```bash
$ mkdir ~/savant
$ cd ~/savant
$ wget http://savant.inversoft.org/org/savantbuild/savant-core/1.0.0/savant-1.0.0.tar.gz
$ tar xvfz savant-1.0.0.tar.gz
$ ln -s ./savant-1.0.0 current
$ export PATH=$PATH:~/savant/current/bin/
```
