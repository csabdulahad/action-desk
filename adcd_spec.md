# Action Desk ADCD Dialog v1

ADCD means **Action Desk Communication Daemon**. It exposes a small local HTTP API that allows scripts, background jobs, and local tools to show native Action Desk dialogs.

## Endpoint

```text
POST http://127.0.0.1:4788/adcd/v1/dialog
```

Default host and port:

| Setting | Default | Notes |
|---|---:|---|
| Host | `127.0.0.1` | Local machine only |
| Network host | `0.0.0.0` | Used when network access is enabled |
| Port | `4788` | Configurable, clamped between `1024` and `65535` |

Request body must be JSON.

Response:

```json
{
  "button": "ok",
  "values": {}
}
```

For invalid requests:

```json
{
  "ok": false,
  "error": "Invalid JSON: ..."
}
```

## Top-level JSON shape

```json
{
  "title": "Action Desk",
  "icon": "info",
  "message": "Hello from ADCD.",
  "sound": "info",
  "fields": [],
  "buttons": {},
  "timeout_ms": 5000,
  "dismissible": true,
  "await_result": true
}
```

## Top-level properties

| Property | Type | Default | Notes |
|---|---|---|---|
| `title` | string | `Action Desk` | Dialog title |
| `icon` | string | `null` | Icon name, resource path, app icon path, or absolute file path |
| `message` | string | `null` | Main message text |
| `sound` | string/object/bool | `null` | Plays a WAV sound once when dialog is shown |
| `fields` | array | `[]` | Form fields or row objects |
| `buttons` | object | default buttons | Button model/configuration |
| `width` | number/string | `auto` | Supports pixels, `auto`, `vw`, e.g. `520`, `"70vw"` |
| `height` | number/string | `auto` | Supports pixels, `auto`, `vh`, e.g. `"60vh"` |
| `min_width` | number | `240` | Minimum dialog width |
| `max_width` | number | `900` | Maximum dialog width |
| `min_height` | number/string | `null` | Optional minimum height |
| `max_height` | number/string | `80vh` | Maximum dialog height |
| `padding` | number/string/array | `0` | Body padding; CSS-like 1-4 values |
| `layout` | object | column layout | Top-level layout config |
| `position` | string | `center` | Passed to Action Desk dialog positioner |
| `dismissible` | bool | `true` | Allows close button, Alt+F4, and Esc to dismiss |
| `always_on_top` | bool | `false` | Keeps dialog above other windows |
| `show_in_taskbar` | bool | `true` | Uses a taskbar-visible frame when true |
| `await_result` | bool | `true` | If false, HTTP returns after the dialog is shown |
| `timeout_ms` | number | see timeout rules | Auto-closes with button `timeout` |

## Sound

`sound` is optional. Missing or `null` means no sound.

Simple form:

```json
{
  "sound": "success"
}
```

Object form:

```json
{
  "sound": {
    "src": "warning"
  }
}
```

Boolean form:

```json
{
  "sound": true
}
```

`true` maps to `default`. `false` disables sound.

### Recommended sound values

These are the recommended built-in/conventional names:

| Value | Expected file |
|---|---|
| `success` | `sound/success.wav` |
| `warning` | `sound/warning.wav` |
| `info` | `sound/info.wav` |
| `error` | `sound/error.wav` |
| `default` | `sound/default.wav`, falling back to `sound/info.wav` |

Disable values:

```text
none, off, no, false, 0, null, ""
```

Example:

```json
{
  "sound": "none"
}
```

### Sound file resolution

ADCD supports WAV only for v1.

For `"sound": "warning"`, ADCD tries:

```text
<APP_FOLDER>/sound/warning.wav
sound/warning.wav from bundled resources
```

For an absolute path, ADCD uses the file directly:

```json
{
  "sound": "C:/Users/ahad/Desktop/warning.wav"
}
```

Use short PCM WAV files. MP3 is not supported in v1.

## Timeout behaviour

Message-only dialogs:

| `timeout_ms` | Behaviour |
|---:|---|
| missing/null | Auto-close after `5000` ms |
| `0` | No auto-close |
| positive number | Auto-close after that many ms |

Dialogs with fields/forms:

| `timeout_ms` | Behaviour |
|---:|---|
| missing/null | No auto-close |
| `0` | No auto-close |
| positive number | Auto-close after that many ms |

When timeout happens:

```json
{
  "button": "timeout",
  "values": {}
}
```

## `await_result`

When `await_result` is `true`, ADCD waits until the user clicks a button, dismisses, or the dialog times out.

When `await_result` is `false`, ADCD returns immediately after showing the dialog:

```json
{
  "button": "shown",
  "values": {}
}
```

This is useful for notification-style dialogs.

## Layout

Top-level layout:

```json
{
  "layout": {
    "direction": "column",
    "gap": 8,
    "padding": 12,
    "align_items": "start"
  }
}
```

Supported layout properties:

| Property | Values | Notes |
|---|---|---|
| `direction` | `column`, `row` | `column` is the practical default |
| `gap` | number | Space between children |
| `padding` | number/string/array | Internal padding |
| `align_items` | `start`, `center` | Useful mainly for row layout |

Padding/margin values support CSS-like shorthand:

```json
{ "padding": 12 }
```

```json
{ "padding": "8 12" }
```

```json
{ "padding": [8, 12, 8, 12] }
```

## Fields

`fields` is a vertical stack. Each item can be a direct field or a row object.

Direct field:

```json
{
  "type": "input",
  "name": "username",
  "label": "Username"
}
```

Row object:

```json
{
  "layout": {
    "direction": "row",
    "gap": 12,
    "align_items": "center"
  },
  "items": [
    { "type": "input", "name": "first_name", "label": "First name" },
    { "type": "input", "name": "last_name", "label": "Last name" }
  ]
}
```

Field-level `margin` controls outside spacing around that field/row.

## Field types

| Type | Purpose | Returns value? |
|---|---|---|
| `input` | Single-line text input | yes |
| `password` | Password input | yes |
| `number` | Text field with numeric validation by default | yes |
| `textarea` | Multi-line text input | yes |
| `select` | Dropdown select | yes |
| `checkbox` | Boolean checkbox | yes |
| `radio` | Radio option group | yes |
| `label` | Static display label | no |
| `text` | Static display text | no |
| `divider` | Horizontal divider | no |
| `image` | Static image/icon | no |

## Common field properties

| Property | Type | Notes |
|---|---|---|
| `type` | string | Field/component type |
| `name` | string | Required if the value should be returned |
| `label` | string | Field label |
| `label_position` | string | `top`, `left`, `right`, `placeholder`, `none` |
| `value` | string | Initial value; for `label`/`text`, this is display text |
| `placeholder` | string | Placeholder text for input fields |
| `checked` | bool | Initial value for checkbox |
| `options` | array | Used by select/radio |
| `width` | number | Fixed component width where supported |
| `height` | number | Fixed component height where supported |
| `src` | string | Image source for `image` field |
| `margin` | number/string/array | Outside spacing |

## Validation properties

| Property | Type | Notes |
|---|---|---|
| `required` | bool | Required field validation |
| `data_type` | string | `string`, `int`, `float`, `bool` |
| `min_len` | number | Minimum string length |
| `max_len` | number | Maximum string length |
| `min` | number | Minimum numeric value |
| `max` | number | Maximum numeric value |
| `allowed_values` | array | Allowed text values |
| `case_sensitive` | bool | Used with `allowed_values` |

For `type: "number"`, default effective data type is `float`.

## Options

String options:

```json
{
  "options": ["low", "medium", "high"]
}
```

Object options:

```json
{
  "options": [
    { "value": "low", "label": "Low" },
    { "value": "high", "label": "High" }
  ]
}
```

## Buttons

Default behaviour:

| Dialog type | Default button model |
|---|---|
| Message-only | `acknowledge` |
| Dialog with fields | `confirm` |

Button models:

| Model | Behaviour |
|---|---|
| `acknowledge` | One neutral button |
| `confirm` | Cancel + submit button |
| `custom` | Uses only `buttons.items` |

Button config:

```json
{
  "buttons": {
    "model": "confirm",
    "layout": "right",
    "gap": 8,
    "show_border": true,
    "confirm": {
      "id": "save",
      "label": "Save",
      "background": "#2563eb",
      "color": "#ffffff"
    },
    "cancel": {
      "id": "cancel",
      "label": "Cancel"
    }
  }
}
```

Button properties:

| Property | Type | Notes |
|---|---|---|
| `id` | string | Returned as `button` |
| `label` | string | Button text |
| `role` | string | `submit`, `cancel`, `neutral` |
| `default` | bool | Sets default/Enter button |
| `color` | string | Text color |
| `background` | string | Background color |
| `padding` | number/string/array | Button padding |
| `font_size` | number | Button font size |
| `border_radius` | number | FlatLaf arc radius |

Button layout values:

```text
left, center, right, fill
```

## Image field

```json
{
  "type": "image",
  "src": "warning",
  "width": 64,
  "height": 64
}
```

Image source can be:

```text
absolute file path
app icons folder file/name
bundled icon resource/name
plain icon name such as warning, success, info, error
```

Supported image formats:

```text
svg, png, jpg, jpeg
```

If one of `width` or `height` is missing, the other value is reused. If both are missing, the default is `64x64`.

## Result values

Submit button on a form returns named field values:

```json
{
  "button": "ok",
  "values": {
    "username": "ahad",
    "remember": true
  }
}
```

Cancel/neutral buttons return empty values:

```json
{
  "button": "cancel",
  "values": {}
}
```

Dismiss returns:

```json
{
  "button": "dismiss",
  "values": {}
}
```

## Examples

### 1. Simple message

```json
{
  "title": "Done",
  "icon": "success",
  "message": "Backup completed successfully.",
  "timeout_ms": 3000,
  "dismissible": true
}
```

### 2. Message with sound

```json
{
  "title": "Warning",
  "icon": "warning",
  "sound": "warning",
  "message": "Disk space is getting low.",
  "buttons": {
    "model": "acknowledge",
    "acknowledge": {
      "label": "OK"
    }
  }
}
```

### 3. Fire-and-forget notification

```json
{
  "title": "Task Started",
  "icon": "info",
  "sound": "info",
  "message": "The sync task has started in the background.",
  "await_result": false,
  "timeout_ms": 2500,
  "show_in_taskbar": false
}
```

Immediate response:

```json
{
  "button": "shown",
  "values": {}
}
```

### 4. Confirm dialog

```json
{
  "title": "Delete file?",
  "icon": "warning",
  "sound": "warning",
  "message": "This file will be moved to trash.",
  "buttons": {
    "model": "confirm",
    "confirm": {
      "id": "delete",
      "label": "Delete",
      "background": "#dc2626",
      "color": "#ffffff"
    },
    "cancel": {
      "id": "cancel",
      "label": "Cancel"
    }
  }
}
```

### 5. Simple input form

```json
{
  "title": "Create user",
  "icon": "user",
  "sound": "info",
  "message": "Enter the new user details.",
  "width": 420,
  "padding": 16,
  "layout": {
    "gap": 10
  },
  "fields": [
    {
      "type": "input",
      "name": "username",
      "label": "Username",
      "required": true,
      "min_len": 3,
      "max_len": 30
    },
    {
      "type": "password",
      "name": "password",
      "label": "Password",
      "required": true,
      "min_len": 8
    },
    {
      "type": "checkbox",
      "name": "enabled",
      "label": "Enable user immediately",
      "checked": true
    }
  ],
  "buttons": {
    "model": "confirm",
    "confirm": {
      "id": "create",
      "label": "Create"
    }
  }
}
```

### 6. Row layout with validation

```json
{
  "title": "Server limit",
  "icon": "server",
  "sound": "info",
  "message": "Set the worker and retry limits.",
  "width": 520,
  "padding": "16 18",
  "layout": {
    "gap": 12
  },
  "fields": [
    {
      "layout": {
        "direction": "row",
        "gap": 12,
        "align_items": "center"
      },
      "items": [
        {
          "type": "number",
          "name": "workers",
          "label": "Workers",
          "required": true,
          "data_type": "int",
          "min": 1,
          "max": 20
        },
        {
          "type": "number",
          "name": "retries",
          "label": "Retries",
          "required": true,
          "data_type": "int",
          "min": 0,
          "max": 10
        }
      ]
    },
    {
      "type": "select",
      "name": "priority",
      "label": "Priority",
      "required": true,
      "options": [
        { "value": "low", "label": "Low" },
        { "value": "normal", "label": "Normal" },
        { "value": "high", "label": "High" }
      ],
      "value": "normal"
    }
  ],
  "buttons": {
    "model": "confirm",
    "confirm": {
      "id": "apply",
      "label": "Apply"
    },
    "cancel": {
      "id": "cancel",
      "label": "Cancel"
    }
  }
}
```

### 7. Advanced mixed content dialog

```json
{
  "title": "Deployment checklist",
  "icon": "build",
  "sound": {
    "src": "success"
  },
  "width": 620,
  "max_height": "80vh",
  "padding": 18,
  "layout": {
    "gap": 12
  },
  "message": "Review the deployment details before continuing.",
  "fields": [
    {
      "layout": {
        "direction": "row",
        "gap": 12,
        "align_items": "center"
      },
      "items": [
        {
          "type": "image",
          "src": "success",
          "width": 48,
          "height": 48
        },
        {
          "type": "text",
          "value": "Build completed. Confirm the environment and release notes."
        }
      ]
    },
    {
      "type": "divider"
    },
    {
      "type": "select",
      "name": "environment",
      "label": "Environment",
      "required": true,
      "options": [
        { "value": "staging", "label": "Staging" },
        { "value": "production", "label": "Production" }
      ],
      "value": "staging"
    },
    {
      "type": "textarea",
      "name": "notes",
      "label": "Release notes",
      "required": true,
      "min_len": 10,
      "height": 120
    },
    {
      "type": "checkbox",
      "name": "confirmed",
      "label": "I have checked the deployment notes",
      "required": true
    }
  ],
  "buttons": {
    "model": "confirm",
    "layout": "right",
    "confirm": {
      "id": "deploy",
      "label": "Deploy",
      "background": "#2563eb",
      "color": "#ffffff",
      "padding": "6 14",
      "border_radius": 8
    },
    "cancel": {
      "id": "cancel",
      "label": "Cancel"
    }
  }
}
```

## PowerShell example

```powershell
$body = @'
{
  "title": "Backup Complete",
  "icon": "success",
  "sound": "success",
  "message": "Backup completed successfully.",
  "timeout_ms": 3000
}
'@

Invoke-RestMethod `
  -Method Post `
  -Uri "http://127.0.0.1:4788/adcd/v1/dialog" `
  -ContentType "application/json" `
  -Body $body
```

## v1 notes

- ADCD Dialog v1 is intentionally practical and form-focused.
- Generic top-level `children` is not part of v1.
- `fields` supports direct fields and row objects.
- `label` and `text` display content using `value`.
- `justify_content` is not part of v1 layout behaviour.
- Sound is WAV-only in v1.
