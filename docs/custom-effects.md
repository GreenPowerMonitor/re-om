# Registering custom effect handlers in re-om

You can register custom **coeffect handlers** using re-om's `register-fx!` function.

`register-fx!` receives two parameters, the first one is the **effect identifier** and the second one is the **effect handler**.

An **effect handler** is a function that performs a side-effect described by an effect.
Remember that [effects are descriptions of side-effects](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/effects-and-coeffects.md), so **effect handlers** are interpreters of those descriptions that will know how to perform the described side-effects.

An effect handler receives only one parameter: **the description of the side-effect to be performed, i.** e., the **effect**.

Example:
```clojure
(re-om/register-fx!
  :om/state
  (fn [[owner mutations]]
    (doseq [[kw value] mutations]
      (om/set-state! owner kw value))
```

In this example we're registering a `:om/state``effect which can be used to mutate values of the local state of an Om control using the keywords those values are associated to.
This effect allows you to change an Om control local state from pure event handlers as shown in the following code snippet:

```clj
(re-om/register-event-handler!
  ::update-state-on-click
  [(re-om/inject-cofx :om/state owner [:selected :expanded])]
  (fn [{{:keys [selected expanded]} :om/state} [owner]]
    {:om/state [owner
                {:selected (not selected)
                 :expanded (not expanded)}]}))
```

In this example, the event handler for the `::update-state-on-click` event returns the `om/state` effect
which describes a mutation of the values associated to the `:selected` and ` :expanded` keywords
in the Om control's state referenced by the `owner` parameter. The value associated to the `om/state` effect in the effects state is a map associating the keywords that will change in the control state to their new values.

Notice also that the previous values of those two keywords were retrieved using a custom coeffect also identified by `:om/state`,
If you want to know how to register custom coeffect handlers, have a look at [Registering custom coeffect handlers in re-om](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/custom-coeffects.md).
