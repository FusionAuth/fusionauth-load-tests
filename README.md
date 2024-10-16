# FusionAuth Load Tests ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)

This repo contains load tests you can run against any FusionAuth instance.

## Setup

### Start FusionAuth

You will need a working FusionAuth deployment. If you want to run load tests, you have probably already figured this part out.

### Configure FusionAuth

In order to run the examples provided in this readme, you'll need to do at least the following:

1. Create an API Key.

   - Go to `https://[your.fusionauth.url]/admin/api-key`.
   - Click the green + button to create a new API Key.
   - On the **Add API Key** screen, don't modify or input any values, just click the Save icon.
   - You should now see your API key. Click on the Key name to reveal the key.
   - Copy that value and set it in your shell.
   ```
   export FA_API_KEY=[your-api-key]
   ```

   Notes:
   * If you don't want to create a Super User key, make sure the key has permissions to add applications, add users, and log in users, for all tenants.
   * If you're a FusionAuth developer, you can run `sb config` to insert an API key into the database.

1. Set the URL of your FusionAuth instance in your shell.
   ```
   export FA_URL=https://[your.fusionauth.url]
   ```

1. Optionally, set the tenant ID for your `Default` tenant.

   If the ID for your `Default` tenant is `efb21cfc-fa60-46f4-9598-889151e58517`, you don't need to set this. If yours is different, you will need to set it.
   ```
   export FA_TENANT_ID=[your-tenant-id]
   ```

1. Run the setup.sh script to create the Application required for the load tests.

   If the script was successful, you should see a new `FusionAuthLoadTesting` application at `https://[your.fusionauth.url]/admin/applications`.



You may also want to change the `factor` in the `User-Registrations.json` file. With no changes, it is `1` which is not very realistic. For a typical FusionAuth deployment, the default is `24000`.

### Configuring Load Tests For a Remote FusionAuth Instance

The load tests are all configured to run against `local.fusionauth.io`. You can run load tests against a FusionAuth instance running at a different hostname, you just need to update the `url` key of each load test JSON file.

```
      "url": "https://local.fusionauth.io",
```


### Build

See the Savant setup below if you do not yet have Savant configured.

```
sb int
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
