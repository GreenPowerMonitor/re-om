(ns greenpowermonitor.re-om
  (:refer-clojure :exclude [get subs])
  (:require
   [om.core :as om :include-macros true]
   [greenpowermonitor.reffectory :as reffect]))

(def ^:private initial-state {:subs {}})

(def ^:private subs-handlers (atom initial-state))

(def ^:private subs-cache (atom {}))

(def ^:private subs (atom {}))

(def ^:dynamic *db* (atom {}))

(defn- get-subs-handler [id]
  (if-let [handler (get-in @subs-handlers [:subs id])]
    handler
    (throw (ex-info
            "Not registered handler!!"
            {:cause :no-handler-registered
             :id id
             :handler-type :subs}))))

(defn get-handler [handler-type id]
  (if (= :subs handler-type)
    (get-subs-handler id)
    (reffect/get-handler handler-type id)))

(def register-cofx! reffect/register-cofx!)

(def register-fx! reffect/register-fx!)

(def inject-cofx reffect/inject-cofx)

(defn register-event-handler!
  ([event-id handler] (register-event-handler! event-id [] handler))
  ([event-id interceptors handler]
   (reffect/register-event-handler!
    event-id
    (conj interceptors (inject-cofx :db nil))
    handler)))

(def dispatch! reffect/dispatch!)

(def dispatch-n! reffect/dispatch-n!)

(defn mutate-db! [db]
  (reset! *db* db))

(register-fx!
 :db
 (fn [db]
   (mutate-db! db)))

(register-cofx!
 :db
 (fn [_ cofx]
   (assoc cofx :db @*db*)))

(defn register-sub! [sub-id f]
  (swap! subs-handlers assoc-in [:subs sub-id] f))

(defn- update-cache [sub-id args result]
  (let [path [sub-id args]
        result-atom (get-in @subs-cache path)]
    (if result-atom
      (reset! result-atom result)
      (swap! subs-cache assoc-in [sub-id args] (atom result)))))

(defn- safe-deref [atom]
  (when atom
    @atom))

(defn subscribe [[sub-id & args] owner]
  (let [handler (get-subs-handler sub-id)]
    (swap! subs update-in [sub-id args] #(conj (set %) owner))
    (or (safe-deref (get-in @subs-cache [sub-id args]))
        (let [result (handler @*db* args)]
          (update-cache sub-id args result)
          result))))

(defn get [[sub-id & args] db]
  (let [handler (get-subs-handler sub-id)]
    (handler db args)))

(def register-events-delegation! reffect/register-events-delegation!)

(defn- any-owner-mounted? [owners]
  (some om/mounted? owners))

(register-event-handler!
 ::init-db
 (fn [_ [db-atom]]
   {:pre [(or (nil? db-atom) (satisfies? IAtom db-atom))]}
   (remove-watch *db* ::watcher)
   (reset! subs-cache {})
   (if (some? db-atom)
     (set! *db* db-atom)
     (mutate-db! {}))
   (add-watch *db*
              ::watcher
              (fn [_ _ old-state new-state]
                (when-not (= old-state new-state)
                  (doseq [[sub-id data] @subs]
                    (let [handler (get-subs-handler sub-id)]
                      (doseq [[args owners] data]
                        (when (any-owner-mounted? owners)
                          (let [result (handler new-state args)]
                            (when (not= result (safe-deref (get-in @subs-cache [sub-id args])))
                              (update-cache sub-id args result))))
                        (doseq [owner owners]
                          (if (om/mounted? owner)
                            (om/refresh! owner)
                            (if (= 1 (count owners))
                              (swap! subs update sub-id #(dissoc % args))
                              (swap! subs update-in [sub-id args] #(disj % owner)))))))))))
   {}))

(defn set-verbose! [verbosity]
  (reset! reffect/verbose verbosity))

(defn get-handlers-state []
  {:subs-handlers @subs-handlers
   :event-handlers @reffect/handlers
   :subs @subs
   :subs-cache @subs-cache})

(defn set-handlers-state! [state]
  (reset! subs-handlers (:subs-handlers state))
  (reset! reffect/handlers (:event-handlers state))
  (reset! subs (:subs state))
  (reset! subs-cache (:subs-cache state)))
