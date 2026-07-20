# FastHopper

**FastHopper** is a Minecraft Paper plugin that increases hopper item transfer speed. Allows customization of items transferred per operation and transfer speed.

## Features

- Configure items transferred per hopper operation (1-64)
- Configurable transfer speed (in ticks)
- Easy `/fh` command with tab-complete
- Vietnamese language support in config
- Performance optimized, prevents item duplication

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/fh set amount <number>` | Set items per transfer | `fasthopper.set` |
| `/fh set cooldown <number>` | Set transfer speed (ticks) | `fasthopper.set` |
| `/fh info` | View current configuration | `fasthopper.info` |
| `/fh reload` | Reload configuration | `fasthopper.reload` |
| `/fh help` | Show help | `fasthopper.admin` |

Alias: `/fasthopper`

## Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `fasthopper.admin` | Admin access to all commands | op |
| `fasthopper.set` | Set transfer amount and speed | op |
| `fasthopper.reload` | Reload configuration | op |
| `fasthopper.info` | View configuration info | op |

## Config (`config.yml`)

```yaml
# Number of items to transfer per operation (1-64)
transfer-amount: 1

# Transfer speed in ticks (1-100, 20 ticks = 1 second)
transfer-cooldown: 8

# Chat prefix
prefix: "&6&l[FastHopper] &r"
```

## Requirements

- Paper 1.21.11 or higher (also supports Folia 1.21.11+)
- Java 21

## Installation

1. Download `FastHopper-1.0.0.jar`
2. Place it in your server's `plugins/` folder
3. Restart the server
4. Edit `plugins/FastHopper/config.yml` if needed


Output JAR will be at `target/FastHopper-1.0.0.jar`.
