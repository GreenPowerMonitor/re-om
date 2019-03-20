# Registering custom coeffect handlers in re-om

You can register custom **coeffect handlers** using the `register-cofx!` function.

A **coeffect handler** is a function that extracts a value from a **coeffect** and associates it to its key in the **coeffects map**.

The last parameter that the **coeffect handler** receives is always the **coeffects map**,
any other parameters to get the value tracked by the **coeffect** should go before it.

Below there's an example of a **coeffect handler** that gets data from the local store.

```clj
(re-om/register-cofx!
   :local-store
   (fn [local-store-key cofx]
    (assoc cofx
           :local-store
           (js->clj (.getItem js/localStorage local-store-key)))))
```

This other **coeffect handler** example is used to get environment variables:

```clj
(re-om/register-cofx!
  :env
  (fn [variables-kws cofx]
    (assoc cofx
           :env
           (if (or (nil? variables-kws) (empty? variables-kws))
              (config/retrieve-env-variables)
              (select-keys (config/retrieve-env-variables) variables-kws)))))
```
where `config/retrieve-env-variables` is a function that gets values from global configuration.

This other **coeffect handler** gets values from the local state of an Om control:

```clj
(re-om/register-cofx!
  :owner
  (fn [owner kws cofx]
    (assoc cofx
           :owner
           (reduce
             (fn [acc k]
               (assoc acc k (om/get-state owner k)))
             {}
             kws))))
```

Because of how `re-om` is designed, coeffect handlers must be **synchronous operations**. If you need to get data using an **asynchronous operation** you must use an **effect** instead.
