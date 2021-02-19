(ns processor.scaler
  (:require
    [clj-http.client :as http]
    [processor.util :as util]

))

(defn body-formatter
  [deployment technology]
  (format "{\"name\":\"%s\", \"technology\":\"%s\"}" deployment technology)
)

(defn scaler-sink
  [configuration]
  (let [host (:host configuration)
      port (:port configuration)]
      (fn [event]
        (let [deployment (util/parse-deployment event)
              technology (util/parse-technology event)
              url        (format "http://%s:%s/api/v1/scale" host port)]

          (http/post url {
            :headers {"Content-Type" "application/json"},
            :body    (body-formatter deployment technology)
          })
))))

(defn scaler-sink-mockup
  [configuration]
  (let [host (:host configuration)
      port (:port configuration)]
      (fn [event]
        (let [deployment (util/parse-deployment event)
              technology (util/parse-technology event)
              url        (format "http://%s:%s/api/v1/scale" host port)]

          (print (format "SINK to %s with d: %s and t: %s." url deployment technology))
))))
