# MIDI Binding

MIDI (Musical Instrument Digital Interface, https://midi.org/) is a protocol for communicating with musical instruments.

This binding lets you react on MIDI events and send MIDI commands to MIDI devices that are on your system.

While this binding can be used to listen messages from instruments and play on them, it is perhaps best suited to enable MIDI controllers to control your home automation system.

## Supported Things

- `MIDI Device`: A device that is conncted to your computer that can communicate over MIDI

## Discovery

MIDI Devices are automatically discovered, and will show up in the inbox.

## Binding Configuration

This binding uses the javax.sound.midi libraries to communicate with local MIDI devices.
No extra configuration is needed.

## Thing Configuration

Things will be auto-discovered. If they are not auto discovered, make sure they show up on the system. 

### Linux

On Linux you can see midi devices like this:

```java
amidi -l
```

If you don't have any MIDI devices, you can add VirMidi (https://alsa.opensrc.org/Virmidi) devices like this:

```java
modprobe snd-virmidi
```

You can use GMIDIMonitor (https://github.com/nedko/gmidimonitor) to inspect sent MIDI messages, and trigger midi messages with VMPK (https://vmpk.sourceforge.io/).

### `MIDI Device` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| deviceId        | text    | Name of the MIDI device in Java MIDI  | N/A     | yes      | no       |

## Channels

Incoming MIDI messages are triggered as strings with "XX XX XX" formatting, where XX are hexadecimal formatted strings.

Sent MIDI messages should be strings formatted with the same "XX XX XX" formatting.

Channel messages whould have 3 or 4 bytes, depending on which message. System Exclusive (SysEx) messages can have unrestricted length.

| Channel               | Type    | Read/Write | Description                                      |
|-----------------------|---------|------------|--------------------------------------------------|
| sendChannelMessage    | String  | W          | Sends 3 or 4 byte channel messages to the device |
| sendSysexMessage      | String  | W          | Send SysEx messages to the device |
| receiveChannelMessage | Trigger | R          | Receive 3 or 4 byte channel messages from the device |
| receiveSysexMessage   | Trigger | R          | Receive SysEx messages from the device  |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```

### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
