# clockify-cli

Command line tool that interacts with [Clockify](https://clockify.me) API.

** Table of Contents**

- [clockify-cli](#clockify-cli)
  - [Installation](#installation)
  - [Usage](#usage)
  - [Examples](#examples)
  - [Configuration](#configuration)

## Installation

Before installing `clockify-cli` one have to have [Java Runtime Environment](https://www.java.com/en/download/) installed.

Clone this repository, `cd` into repo dir and run `./install.sh`.

**Windows** users can use [WSL](https://docs.microsoft.com/en-us/windows/wsl/install-win10) to run the script or use Java Runtime Environment to run it:

- download the JAR file from [releases](https://github.com/gawliks/clockify-cli/releases/latest/download/clockify-cli-standalone.jar)
- run `java -jar clockify-cli-standalone.jar` in the directory where the JAR file is located

### Before first run

Check [Configuration](#configuration) section to see how to setup the config file.

## Usage

```
clockify-cli [options] [command] [arguments]

General options:

-h --help Show help

Commmands:

help: Show help

time-entry [description]: Add time entry with provided description (or default one from config file)
  Options:
    -p --project PROJECT Project to add time entry to
    -w --workspace WORKSPACE Workspace to add time entry to
    -s --start START-TIME Start time of time entry
    -e --end END-TIME End time of time entry
    -d --day (today | yesterday | custom date YYYY-MM-DD) Date of time entry

  If no options are given, the default [workspace, project, start, end, day] are used
  If no description is given, the default description (Working on [project]) is used

workspaces: List all workspaces

projects [workspace]: List all projects in provided workspace (or default one from config file)

```

## Examples

adding time entry for yesterday:

`clockify-cli -w "My workspace" -p "My Project" -s 9:00 -e 17:00 -d yesterday time-entry "Doing stuff"` (check [Configuration](#configuration) section to see how to do it simpler without repeating yourself)

listing your workspaces:

`clockify-cli workspaces`

listing projects in workspace:

`clockify-cli projects "My workspace"`

## Configuration

In order to have `clockify-cli` working one have to setup a simple configuration that is stored in `~/.clockify/config` file.

The only requirement is to generate and define **clockify API key**. It can be generated in [Clockify settings](https://clockify.me/user/settings) in API section.

Init your config with `clockify-cli config init`

then set api key with `clockify-cli config set clockify-api-key [YOUR_API_KEY]`

The rest of the config entries are optional and are used as defaults for `clockify-cli` commands. This way one can simplify the usage of the tool by not providing the same options over and over again.

### Example config file:

```
{:clockify-api-key "YOUR_API_KEY_HERE"
 :workspace "My workspace"
 :project "Project you are working on"
 :start "09:00"
 :end "17:00"
 :day "today"}
```

### Other examples:

To set the workspace run `clockify-cli config set workspace "My new workspace"`

To change the day run `clockify-cli config set day yesterday` or `clockify-cli config set day "2020-01-01"` for a custom date.

When your config file is ready, you can add time entry with `clockify-cli time-entry "Doing stuff"`

## License

See [LICENSE.md](./LICENSE.md) file.
