## FusionAuth Load Tests ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)

## Examples Usages:

### Build

See the Savant setup below if you do not yet have Savant configured. 

```
sb int
```

### Setup FusionAuth

If you want to run load tests, you have probably already figured this part out.

### Configure FusionAuth for load tests
 
All of this is optional and will depend upon what you want your tests to do. However, to use the provided examples here, start with these setup steps. 

1. Setup an API key. 
   - If you're a FusionAuth developer, run `sb config` which will insert an API key into the database. 
   - If you're just an everyday average FusionAuth enthusiast, add an API key however you know how. The examples will expect an API key of `bf69486b-4733-4470-a592-f1bfce7af580` but this can be modified to your liking. 
   - The key must be a super user key or have permissions to add users and applications as well as log users in. It must work for all tenants.
2. Set up a tenant. The examples will expect a tenant id of `efb21cfc-fa60-46f4-9598-889151e58517` but that can be modified if you want.
3. Setup an Application
   - If needed, update `./src/main/script/setupApplication.sh`. Change the name of the host where FusionAuth runs, if not running it locally.
   - Run `./src/main/script/setupApplication.sh`  

You may also want to change the `factor` in the `User-Registrations.json` file. With no changes, it is `1` which is not very realistic. For a typical FusionAuth deployment, the default is `24000`.

### Configuring Load Tests For a Remote FusionAuth Instance

The load tests are all configured to run against `local.fusionauth.io`. You can run load tests against a FusionAuth instance running at a different hostname, you just need to update the `url` key of each load test JSON file.

```
      "url": "https://local.fusionauth.io",
```

### Run a load test

Before you can log users in, you need to create users. This load test will create users with a registration. 

Register users, see `src/main/resources/User-Registrations.json` for the test definition.

````
cd build/dist
./load-test.sh User-Registrations.json
````


Run login tests. See `src/main/resources/User-Logins.json` for the test definition.

````
cd build/dist
./load-test.sh User-Logins.json
````

### Cleanup

You can delete the load testing tenant you created and all the users, applications and registrations will be removed.

### Hacking

Feel free to review how the test harness is configured using the JSON DSL. You can add more workers, and worker directives if you like. If you make something cool, feel free to submit a PR.

If you want to dig into the code further, the `FusionAuthWorkerFactory` is what takes a `directive` from the `Foreman` and builds a worker to satisfy the directive.  

### Building with Savant

**Note:** This project uses the Savant build tool. To compile using Savant, follow these instructions:

```bash
mkdir ~/savant
cd ~/savant
wget http://savant.inversoft.org/org/savantbuild/savant-core/2.0.0-RC.7/savant-2.0.0-RC.7.tar.gz
tar xvfz savant-2.0.0-RC.7.tar.gz
ln -s ./savant-2.0.0-RC.7 current
export PATH=$PATH:~/savant/current/bin/
```

Then, perform an integration build of the project by running:

```bash
$ sb int
```
