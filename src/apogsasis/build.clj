(ns apogsasis.build
  (:require [apogsasis.core :as core]))

(defn -main []
  (println "Building static site...")
  (core/export-site)
  (println "Site built successfully in dist/"))