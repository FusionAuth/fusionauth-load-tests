# FusionAuth Load Tests ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)

This repo contains load tests you can run against any FusionAuth instance.


## Setup

### Start FusionAuth

You will need a working FusionAuth instance. If you want to run load tests, you have probably already figured this part out.

### Configure FusionAuth

#### Create an API Key

1. Go to `https://[your.fusionauth.url]/admin/api-key`.
1. Click the green + button to create a new API Key.
1. On the **Add API Key** screen, don't modify or input any values, just click the Save icon.
1. You should now see your API key. Click on the Key name to reveal the key.

⚠️ If you don't want to create a Super User key, make sure the key at least has permissions to add applications, add users, and log in users, for all tenants.

#### Run the setup script

Run the `setup.sh` script to create the FusionAuth Application required for the load tests.

All parameters are optional and will default to values used by FusionAuth's internal development team. If you're not on our development team (and maybe even if you are, if you're not running FusionAuth locally, or not using default values) you'll have to pass in at least `--url` and `--key`:
```
./setup.sh --url https://[your.fusionauth.url] --key [your-api-key]
```

⚠️ If your `Default` tenant Id is different from what the script expects, you may get a `400` response code when running the script. In this case, get the Id from your `Default` tenant and pass that in with the `--tenant` flag.

If the script was successful, you should see a new **FusionAuthLoadTesting** Application in your FusionAuth instance's Admin portal.


### Configure Load Tests

The setup script will configure two common tests for you, [User-Registrations](src/main/resources/User-Registrations.json) and [User-Logins](src/main/resources/User-Logins.json). If you want to run other tests, you'll need to modify them with your authentication data.

Check the test JSON files in [src/main/resources](src/main/resources). Update each JSON file you want to use with your URL, api key, and tenant Id value as needed.

You may also wish to adjust the load test run by modifying the `loopCount` and `workerCount` values.

To test user registrations with less CPU load, you can change the `factor` value in [User-Registrations](src/main/resources/User-Registrations.json). The default of `24000` is meant to mimic an actual user registration. You can turn that down as low as `1` to allow for faster registrations with less CPU load. This can be useful if you need to hit the database hard without being constrained by CPU.


### Building the Tests

The setup script will build the tests for you. However, if you've made changes and need to rebuild, run `sb int`.

You can also start from scratch and wipe the `build` directory with `sb clean`.

See [Installing Savant](#installing-savant) below if you do not have Savant installed.


## Run a Load Test

Before you can log users in, you need to create users.

⚠️ **IMPORTANT!** If you have configured SMTP in your Tenant with a valid public SMTP server, performing a user registration test could get you flagged for abuse, and negatively impact your email deliverability. You should remove your SMTP configuration before running the user registration test.

To create users, run the User-Registrations test:
```
cd build/dist
./load-test.sh User-Registrations.json
```

Now that you have users, you can run the User-Logins test:
```
cd build/dist
./load-test.sh User-Logins.json
```

## Tuning a Load Test

The load test spins up individual Worker threads as specified in the configuration file. Each Worker synchronously makes repeated API requests. If your request latency is high (e.g., running a remote instance), this can limit your maximum potential throughput. Increasing the number of worker threads alleviates this (unless you hit local system limitations).

For example, if the request latency is 250 ms and you have 10 worker threads, you'll have a max throughput of 40 requests per second. Increasing the number of worker threads to 100 increases the max throughput to 400 requests per second.

## Cleanup

If your load test created a Tenant, delete the Tenant it created.

If your load test only created an Application under the Default Tenant, delete the `FusionAuthLoadTesting` Application to delete the users created by this load test.


## Hacking

Feel free to review how the test harness is configured using the JSON DSL. You can add more workers, and worker directives if you like. If you make something cool, feel free to submit a PR.

If you want to dig into the code further, the `FusionAuthWorkerFactory` is what takes a `directive` from the `Foreman` and builds a worker to satisfy the directive.


## Installing Savant

This project uses the [Savant](https://github.com/savant-build/savant-core) build tool.

To install Savant, follow these instructions:
```bash
mkdir ~/savant
cd ~/savant
wget http://savant.inversoft.org/org/savantbuild/savant-core/2.0.0/savant-2.0.0.tar.gz
tar xvfz savant-2.0.0.tar.gz
ln -s ./savant-2.0.0 current
export PATH=$PATH:~/savant/current/bin/
```
