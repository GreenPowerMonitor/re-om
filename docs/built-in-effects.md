# Built-in effects in re-om

## :db

The `:db` effect is used to reset the whole `app-state`.

The data associated to a `:db` effect is **the new value of the `app-state`**.

The event handler for the `::initialize` event below is resetting the whole `app-state` (which it gets through its parameters thanks to the `:db` **coeffect**).

```clojure
(re-om/register-event-handler!
 ::initialize
 (fn [{:keys [db]} [id checked enabled dragging]]
   {:db (update-in db [::state id] merge {:checked checked
                                          :enabled enabled
                                          :dragging dragging})}))
```

## :dispatch
The `:dispatch` effect is **used to dispatch an event**.

The data associated to a `:dispatch` effect is a **vector** whose **first element** is the
keyword that identifies the event that will be dispatched. The **rest of elements of the vector**
are the **event payload** and they are optional.

In the following example, the event handler for the ` ::show-alert-on-rim` event handler evaluates to an **effect map** containing a `:dispatch` effect that will dispatch the `::domain.rim/show-alert-on-rim` with a **payload** that will be a vector of 3 elements resulting from evaluating  `(:alert-id value) `, `(:alarm value)` and `time-period`.

```clojure
(re-om/register-event-handler!
 ::show-alert-on-rim
 (fn [_ [value time-period]]
   {:dispatch [::domain.rim/show-alert-on-rim (:alert-id value) (:alarm value) time-period]}))
```

In the following case the payload of the dispatched event, `::svg.fetch`, is empty:

```clojure
(re-om/register-event-handler!
 ::fetch-data
 (fn [_ _]
   {:dispatch [::svg.fetch]}))
```

## :dispatch-n
The `:dispatch-n` effect is **used to dispatch a sequence of events in order**.

In the following example the event handler for the `::page-change` event evaluates to an effect map that includes a `:dispatch-n` effect  which will dispatch the `::meta.fetch` event with a payload of `[35]` and a `::svg.fetch` event with an empty payload.

```clojure
(re-om/register-event-handler!
 ::page-change
 (fn [_ _]
   {:dispatch-n [[::meta.fetch 35]
                 [::svg.fetch]]}))
```


## :dispatch-later
The `:dispatch-later` effect is **used to dispatch an event after some milliseconds**.

The data associated to a `:dispatch` effect is a **map** whose value for the `:ts` key is the number of milliseconds the dispatch will be delayed, and the value for the `:events` keyword is the event that will be dispatched. The event is represented in the same way all evebnt

In the following example the event handler for the `::remove-calendar-with-delay!` event evaluates to an effect map that includes a `:dispatch-later` effect  which will dispatch after `calendar-remove-delay` the `::remove-calendar!` event with a payload of `[calendar-remove-fn]`.

```clojure
(re-om/register-event-handler!
 ::remove-calendar-with-delay!
 (fn [_ [calendar-remove-fn]]
   {:dispatch-later {:ts calendar-remove-delay-ms
                              :event [::remove-calendar! calendar-remove-fn]}}))
```

The `make-update-new-cards-values-succeeded-event-handler` function shown below evaluates to an event handler which is a very interesting case because when the condition in the `if` evaluates to `false`, it dispatches itself after several milliseconds (recursion).:

```clojure
(defn make-update-new-cards-values-succeeded-event-handler [values-state-lens values-data-lens event-id]
  (fn [{:keys [db]} [state data]]
    (if (= :finished (:config-state state))
      {:db (domain.cards/update-card db (domain.cards.transformation/transform-cards data state))}
      {:dispatch-later {:ts retry-time :event [event-id data]}})))
```
