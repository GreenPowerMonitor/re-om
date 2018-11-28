# Built-in Coeffects in re-om

## :db

The `:db` coeffect is used to retrieve the whole `app-state`.

The data associated to a **`:db` coeffect is a map that is the current value of the whole `app-state`**.

The `:db` coeffect is automatically injected by `re-om` into the **coeffects map** so you don't need to inject this coeffect when we define an event handler,

The event handler for the `::set-screen-size` event below is getting the current `app-state` inside the **coeffects map** (the first  parameter of any event handler) thanks to the `:db` **coeffect** and then resetting the `app-state` using the `:db` **effect**.

```clojure
(re-om/register-event-handler!
 ::set-screen-size
 (fn [{:keys [db]} [size]]
   {:db (assoc db ::screen-size size)}))
```