# SETUP A DEBIAN ONLINE SERVER

Using a VPS provider or a Cloud Provider, just spin the cheapest Linux server they allow running on Debian or Ubuntu, because after you finish this demo you will throw it away, therefore it will cost you only a few cents.

You will also need to create a sub domain on a domain you own, that you will point to this new server, like `dev.example.com`. You can also throw it away after you are done with this demo.

#### Update the New Server

Assuming that you have a new brand server you should have now a shell as the `root` user, thus let's get it up to date with:

```
apt update && apt -y upgrade
```

#### Create Unprivileged User

We will not run the demo as `root`, because it's a best security practice to not run as `root`.

Check if the server already have an unprivileged user:

```
grep -irn :1000: /etc/passwd
```

Output example for a server that already has one:

```
28:debian:x:1000:1000:Cloud-init-user,,,:/home/debian:/bin/bash
```

If you don't get any output, then it means it doesn't exist yet, thus you can add a new unprivileged user with:

```
adduser debian
```
> **NOTE**: Type you password and reply to all other questions with just hitting `enter`.

Add the user to `sudo` with:

```
usermod -aG sudo debian
```

Switch to the `debian` user with:

```
su - debian
```

#### Clone the Shipfast Repository

We need `git` for this:

```
sudo apt install -y git
```

Now we can clone the repo with:

```bash
git clone https://github.com/approov/shipfast-api-protection.git && cd shipfast-api-protection
```

#### Server Setup

This will install Docker, Docker Compose, and Traefik running on a Docker container.

This setup will use a `docker-compose.yml` file to setup a [Traefik](https://docs.traefik.io/) reverse proxy on port `80` and `443` for all docker containers running in the same host machine, therefore you cannot setup this in an existing server.

> **NOTE:** Traefik is being used to automated the process of using `https` for the Shipfast API, because it will auto generate the TLS certificates, meaning zero effort from you to have `https`.

##### Traefik `.env` file

```
cp ./traefik/.env.example ./traefik/.env
```

Now edit `./traefik/.env` to update the place-holder values with your own values:

```
nano ./traefik/.env
```

##### Run the Setup

```
sudo ./bin/setup-online-server.sh
```

#### Shipfast API and Shipraider Web Setup

##### Copy the Env File from your Computer

From your local computer run:

```
scp .env root@my-online-server-ip-or-domain:/home/debian/shipfast-api-protection
```

Confirm it exists in the online server:

```
ls -a | grep .env -
```

output should be like:

```
.env
.env.example
```

#### Building the Docker Images

```
./shipfast build servers
```

#### Running the ShipFast API and ShipRaider Web

Bring up with:

```
./shipfast up servers
```

Tail the logs with:

```
./shipfast logs servers
```

Restart with:

```
./shipfast restart servers
```

Bring down with:

```
./shipfast down servers
```

> **NOTE:** you can handle just the API server or the Web server by replacing `server` with `api` or `web`, like `./shipfast logs api`.
