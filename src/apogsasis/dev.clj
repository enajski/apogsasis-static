(ns apogsasis.dev
  (:require [apogsasis.core :as core]
            [stasis.core :as stasis]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]))

(defn wrap-static-resources [handler]
  (-> handler
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified))

(defn -main []
  (println "Starting development server on http://localhost:3000")
  (let [pages-handler (stasis/serve-pages core/get-pages {:optimus-bundles []})
        handler (wrap-static-resources pages-handler)]
    (jetty/run-jetty handler {:port 3000 :join? true})))

