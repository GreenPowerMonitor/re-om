# Built-in events in re-om

## ::init-db

The handler of this event can receive as its payload a var referencing the atom
that holds the app-state of the legacy SPA in which you want to start using re-om.

We recommend to dispatch this event before mounting the root component of your SPA
in order to synchronize the state used by your legacy code and
the one used by re-om for subscriptions, the `:db` effects and the `:db` coeffect,
or any other effect ot coeffect you define that reads or modifies the app-state.

This is a very important characteristic of re-om because it allows to incrementally
introduce it in legacy applications following the [Strangler Application pattern](https://www.martinfowler.com/bliki/StranglerApplication.html).
See the post [Giving new life to existing Om legacy SPAs with re-om](https://codesai.com/2018/10/re-om)
to know more about why we followed this approach.

re-om will keep a reference to your application's app-state
and add a watcher to it that will be in charge of refreshing Om components
any time there's a change in the value computed by a re-om's subscription
declared in them.

Example:

```clj
(defn initialize-re-om! [app-state]
  (if (env/dev?)
    (re-om/set-verbose! true)
    (re-om/set-verbose! false))
  (re-om/dispatch! [::re-om/init-db app-state]))
```

If you don't pass any atom in the event payload, re-om will initialize the app-state with an atom holding an empty map.
This means you can also write green field SPAs using re-om, but we don't recommend it because nowadays
Om is not a good choice anymore. If you want to start a green field SPA, we recommend you to use [re-frame](https://github.com/Day8/re-frame) instead.



