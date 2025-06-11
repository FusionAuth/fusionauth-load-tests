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

#### Configuring FusionAuth Tenants, Applications, and Users

If you want to run the load tests against a specific Tenant, Application, or User, you can modify the JSON files in [src/main/resources](src/main/resources) to include the `tenantId`, `applicationId` values. This is the legacy configuration in `User-Registrations.json` and `User-Logins.json`.

If you want to run load tests against a set of users divided amongst applications and tenants, then don't set a specific `applicationId` and `tenantId`. Instead, setting the `numberOfApplications` and `numberOfTenants` values will allow you to create a set of users that are divided amongst the specified number of applications and tenants following these steps:

1. Run `Create-Tenants.json` to create the desired number of tenants.
2. Run `Create-Applications.json` with `numberOfTenants` set to create the desired number of applications distributed across the tenants. 
3. Run `User-Registrations-Multi.json` with `numberOfApplications` and `numberOfTenants` set to create the desired number of users distributed across the applications and tenants. 

The names of tenants, applications, and users will be of the formats: `tenant_{n}`, `application_{n}`, and `load_user_[n]@fusionauth.io`, where `[n]` is a decimal number starting from 1.

The UUIDs are hexadecimal and follow a 1-based index modulus pattern so we can deterministically determine which tenantId and applicationId to provide.
An example set of users with 10 tenants, 1000 applications, and 10,000 users would produce entries like this:

| User Email                   | Application Id                     | Tenant Id                          |
|------------------------------|------------------------------------|------------------------------------|
| load_user_1@fusionauth.io    | 00000000-0000-0001-0000-00000001   | 00000000-0000-0000-0000-00000001   |
| load_user_2@fusionauth.io    | 00000000-0000-0001-0000-00000002   | 00000000-0000-0000-0000-00000002   |
| load_user_3@fusionauth.io    | 00000000-0000-0001-0000-00000003   | 00000000-0000-0000-0000-00000003   |
| ...                          |                                    |                                    |
| load_user_9@fusionauth.io    | 00000000-0000-0001-0000-00000009   | 00000000-0000-0000-0000-00000009   |
| load_user_10@fusionauth.io   | 00000000-0000-0001-0000-0000000a   | 00000000-0000-0000-0000-0000000a   |
| load_user_11@fusionauth.io   | 00000000-0000-0001-0000-0000000b   | 00000000-0000-0000-0000-00000001   |
| ...                          |                                    |                                    |
| load_user_99@fusionauth.io   | 00000000-0000-0001-0000-00000063   | 00000000-0000-0000-0000-00000009   |
| load_user_100@fusionauth.io  | 00000000-0000-0001-0000-00000064   | 00000000-0000-0000-0000-0000000a   |
| ...                          |                                    |                                    |
| load_user_1000@fusionauth.io | 00000000-0000-0001-0000-000003e8   | 00000000-0000-0000-0000-0000000a   |
| load_user_1001@fusionauth.io | 00000000-0000-0001-0000-00000001   | 00000000-0000-0000-0000-00000001   |
| ...                          |                                    |                                    |

The `User-Logins-Multi.json` configuration will test the login endpoint with these users.

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
# optionally run ./set-url-host.sh <new_host> to change the host in the JSON files
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
