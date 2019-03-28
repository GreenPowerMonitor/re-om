## register-cofx!
See [register-cofx! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#register-cofx)

## register-fx!
reffect/register-fx!)

## inject-cofx
See [register-cofx! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#inject-cofx)

## register-event-handler!
  ([event-id handler] (register-event-handler! event-id [] handler))
  ([event-id interceptors handler]
   (reffect/register-event-handler!
    event-id
    (conj interceptors (inject-cofx :db nil))
    handler)))

## interceptor
reffect/interceptor)

## dispatch!
reffect/dispatch!)

## dispatch-n!
reffect/dispatch-n!)

## mutate-db!
[db]

## Built-in effects
:db

## Built-in coeffects
:db

## register-sub!
[sub-id f]

## subscribe
[[sub-id & args] owner]

## get
[[sub-id & args] db]


## register-events-delegation!
reffect/register-events-delegation!)


## `set-verbose!`
[verbosity]
reffect/verbose verbosity

## Only for testing purposes

### get-handler
[handler-type id]
  (if (= :subs handler-type)
    (get-subs-handler id)
    (reffect/get-handler handler-type id)))

### `get-handlers-state`

### `set-handlers-state!'
[state]
