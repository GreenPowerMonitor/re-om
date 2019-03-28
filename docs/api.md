## dispatch!
It delegates on reffectory.
See [dispatch! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#dispatch).

## `dispatch-n!`
It delegates on reffectory.
See [dispatch-n! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#dispatch).

## `register-event-handler!
It delegates on reffectory.`
  ([event-id handler] (register-event-handler! event-id [] handler))
  ([event-id interceptors handler]
   (reffect/register-event-handler!
    event-id
    (conj interceptors (inject-cofx :db nil))
    handler)))
    
## `inject-cofx`
It delegates on reffectory.
See [inject-cofx in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#inject-cofx).

## `register-cofx!`
It delegates on reffectory.
See [register-cofx! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#register-cofx).

## `register-fx!`
It delegates on reffectory.
See [register-fx! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#register-fx).

## `register-events-delegation!`
It delegates on reffectory.
See [register-events-delegation! in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#register-events-delegation).

## `interceptor`
It delegates on reffectory.
See [interceptor in reffectory](https://github.com/GreenPowerMonitor/reffectory/blob/master/docs/api.md#interceptor).

## `mutate-db!`
[db]

## `register-sub!`
[sub-id f]

## `subscribe`
[[sub-id & args] owner]

## `get`
[[sub-id & args] db]

## `set-verbose!`
[verbosity]
reffect/verbose verbosity

## Only for testing purposes

### `get-handler`
This function is used only in tests and gets handlers registered in reffectory.

It receives two parameters: the handler type and the identifier of the thing (event, effect, coeffect or subscription) the handler is associated with.

Example:
```clj
(let [subscribe (get-handler :event-fns ::real-time-data/subscribe) ;; <- this extracts an event handler
       extract-om-state (get-handler :cofxs :om/state) ;; <- this extracts a coeffect handler
       mutate-om-state (get-handler :fxs :om/state) ;; <- this extracts an effect handler
       todos-subscription (get-handlers :subs ::todos-subscription)] ;; <- this extracts a subscription
 ;; doing something with them in tests
)
```

### `get-handlers-state`
[]

### `set-handlers-state!'
[state]
