(ns apogsasis.core
  (:require [stasis.core :as stasis]
            [hiccup2.core :as hiccup]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def base-path 
  (let [env-base-path (System/getenv "BASE_PATH")
        jvm-base-path (System/getProperty "BASE_PATH")]
    (cond
      (= env-base-path "none") ""
      env-base-path env-base-path
      jvm-base-path jvm-base-path
      :else "")))

(defn url [path]
  (str base-path path))

(defn load-data [filename]
  (-> (str "resources/data/" filename)
      slurp
      edn/read-string))

(defn load-releases []
  (load-data "releases.edn"))

(defn load-videos []
  (load-data "videos.edn"))

(defn get-release-by-id [releases catalog-id]
  (first (filter #(= (:catalog-id %) catalog-id) releases)))

(defn get-video-by-name [videos video-name]
  (first (filter #(= (:name %) video-name) videos)))

(defn render-html [content]
  (str "<!DOCTYPE html>\n" (hiccup/html content)))

(defn base-layout [title content]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title title]
    [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/@webtui/css@latest/dist/full.css"}]
    [:link {:rel "stylesheet" :href (url "/css/nav-tree.css")}]
    [:link {:rel "icon" :type "image/x-icon" :href (url "/favicon.ico")}]]
   [:body
    content]])

(defn group-releases-by-series [releases]
  (let [grouped (group-by #(cond
                             (str/starts-with? (:catalog-id %) "aposet") "Sets"
                             (or (str/starts-with? (:catalog-id %) "mvapo")
                                 (= (:catalog-id %) "SRmp3_081_Rekombinacja_-_Notoryczni_Wyznawcy_Ewidentnych_Uproszczen")) "Collabs"
                             :else "Main Catalog") releases)]
    (into {} (map (fn [[k v]] [k (sort-by :catalog-id v)]) grouped))))

(defn sidebar [releases videos current-item item-type]
  [:aside {:style "width: 320px; padding: 1rem; border-right: 1px solid var(--border); height: 100vh; overflow-y: auto; font-family: var(--font-mono, monospace);"}
   [:div
    [:h2 {:style "margin-bottom: 1.5rem; font-size: 1.25rem;"} "apogsasis"]

    [:nav
     [:details {:open (= item-type :release) :style "margin-bottom: 1rem;"}
      [:summary {:style "cursor: pointer; padding: 0.5rem; border-radius: 4px; font-weight: 600; list-style: none; user-select: none; display: flex; align-items: center; gap: 0.5rem;"}
       [:span {:style "font-size: 0.75rem;"} "▸"]
       [:span "♪ Music"]
       [:span {:style "opacity: 0.6; font-size: 0.875rem;"} (str "(" (count releases) ")")]]

      (let [grouped-releases (group-releases-by-series releases)]
        [:div {:style "margin-left: 1rem; margin-top: 0.5rem;"}
         (for [[section-name section-releases] (sort-by first grouped-releases)]
           [:details {:open (and (= item-type :release)
                                 (some #(= (:catalog-id %) (:catalog-id current-item)) section-releases))
                      :style "margin-bottom: 0.5rem;"}
            [:summary {:style "cursor: pointer; padding: 0.25rem; font-size: 0.875rem; opacity: 0.8; list-style: none; user-select: none; display: flex; align-items: center; gap: 0.25rem;"}
             [:span {:style "font-size: 0.6rem;"} "▸"]
             [:span section-name]
             [:span {:style "opacity: 0.6; font-size: 0.75rem;"} (str "(" (count section-releases) ")")]]
            [:div {:style "margin-left: 1rem; margin-top: 0.25rem;"}
             (for [release section-releases]
               [:a {:href (url (str "/releases/" (:catalog-id release) ".html"))
                    :style (str "display: block; padding: 0.375rem 0.5rem; margin-bottom: 0.125rem; text-decoration: none; border-radius: 3px; font-size: 0.8rem; line-height: 1.3; "
                                (when (= (:catalog-id release) (:catalog-id current-item)) "background-color: var(--surface); font-weight: 600; "))}
                [:div
                 [:span {:style "opacity: 0.6; margin-right: 0.5rem; font-family: var(--font-mono, monospace);"} (:catalog-id release)]
                 [:span (:group release)]
                 [:div {:style "font-size: 0.7rem; opacity: 0.7; margin-top: 0.125rem;"} (:title release)]]])]])])]

     [:details {:open (= item-type :video) :style "margin-bottom: 1rem;"}
      [:summary {:style "cursor: pointer; padding: 0.5rem; border-radius: 4px; font-weight: 600; list-style: none; user-select: none; display: flex; align-items: center; gap: 0.5rem;"}
       [:span {:style "font-size: 0.75rem;"} "▸"]
       [:span "▶ Videos"]
       [:span {:style "opacity: 0.6; font-size: 0.875rem;"} (str "(" (count videos) ")")]]

      [:div {:style "margin-left: 1rem; margin-top: 0.5rem;"}
       (for [video videos]
         [:a {:href (url (str "/videos/" (str/replace (:name video) #"\s+" "-") ".html"))
              :style (str "display: block; padding: 0.375rem 0.5rem; margin-bottom: 0.125rem; text-decoration: none; border-radius: 3px; font-size: 0.8rem; "
                          (when (= (:name video) (:name current-item)) "background-color: var(--surface); font-weight: 600; "))}
          (:name video)])]]]]])

(defn release-content [release]
  [:div {:style "display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; margin-bottom: 2rem;"}
   [:div
    (when-let [cover (first (:covers release))]
      [:img {:src (url (:url cover)) :alt (:title release)
             :style "width: 100%; height: auto; border-radius: 8px; margin-bottom: 1.5rem;"}])
    (when (:release-notes release)
      [:div
       [:h3 {:style "margin-bottom: 1rem;"} "Release Notes"]
       [:p {:style "line-height: 1.6;"} (:release-notes release)]])]
   [:div
    [:h1 {:style "margin-bottom: 0.5rem;"} (:title release)]
    [:h2 {:style "margin-bottom: 2rem; opacity: 0.7; font-weight: normal;"} (:group release)]

    (when (:tracks release)
      [:div {:style "margin-bottom: 2rem;"}
       [:h3 {:style "margin-bottom: 1rem;"} "Tracks"]
       [:ol {:style "padding-left: 1.5rem;"}
        (for [track (:tracks release)]
          [:li {:style "margin-bottom: 0.5rem;"} (:title track)])]])

    [:div {:style "margin-bottom: 2rem;"}
     [:h3 {:style "margin-bottom: 1rem;"} "Listen"]

     ;; Archive.org player (using catalog-id like original Meteor app)
     (when (:catalog-id release)
       [:div {:style "margin-bottom: 1rem;"}
        [:h4 {:style "margin-bottom: 0.5rem; font-size: 0.9rem; opacity: 0.8;"}
         "Archive.org "
         [:a {:href (str "https://archive.org/details/" (:catalog-id release))
              :target "_blank"
              :style "opacity: 0.6; text-decoration: none;"} "↗"]]
        [:iframe {:src (str "https://archive.org/embed/" (:catalog-id release) "&playlist=1")
                  :width "100%" :height "300" :frameborder "0"
                  :allowfullscreen true
                  :style "border-radius: 8px; margin-bottom: 1rem;"}]])

     ;; Spotify player
     (when (:spotify-url release)
       [:div {:style "margin-bottom: 1rem;"}
        [:h4 {:style "margin-bottom: 0.5rem; font-size: 0.9rem; opacity: 0.8;"} "Spotify"]
        [:iframe {:src (str "https://open.spotify.com/embed/album/"
                            (last (str/split (:spotify-url release) #":")))
                  :width "100%" :height "352" :frameborder "0" :allowtransparency "true"
                  :style "border-radius: 8px;"}]])]

    (when (:downloads release)
      [:div
       [:h3 {:style "margin-bottom: 1rem;"} "Downloads"]
       (for [download (:downloads release)]
         [:div {:style "margin-bottom: 1rem;"}
          [:a {:href (:url download)
               :style "display: inline-block; padding: 0.5rem 1rem; border: 1px solid var(--border); border-radius: 4px; text-decoration: none; transition: all 0.2s ease;"}
           "⬇ " (:name download)]
          (when (:description download)
            [:br]
            [:small {:style "opacity: 0.6; margin-top: 0.25rem; display: block;"} (:description download)])])])]])

(defn video-content [video]
  [:div
   [:h1 {:style "margin-bottom: 2rem;"} (:name video)]
   [:div {:style "border: 1px solid var(--border); border-radius: 8px; overflow: hidden; margin-bottom: 2rem;"}
    [:iframe {:src (str "https://player.vimeo.com/video/" (:vimeo-id video))
              :width "100%" :height "500" :frameborder "0" :allowfullscreen true
              :style "display: block;"}]]])

(defn home-page [releases videos]
  (let [latest-release (last releases)]
    (base-layout
     "apogsasis"
     [:section.vbox.bg-black
      [:section.hbox.stretch
       (sidebar releases videos latest-release :release)
       [:section#content.bg-light.lter
        (release-content latest-release)]]])))

(defn release-page [releases videos catalog-id]
  (let [release (get-release-by-id releases catalog-id)]
    (base-layout
     (str (:title release) " - " (:group release))
     [:div {:style "display: flex; min-height: 100vh;"}
      (sidebar releases videos release :release)
      [:main {:style "flex: 1; padding: 2rem; overflow-y: auto;"}
       (release-content release)]])))

(defn video-page [releases videos video-name]
  (let [video (get-video-by-name videos video-name)]
    (base-layout
     (str (:name video) " - apogsasis")
     [:div {:style "display: flex; min-height: 100vh;"}
      (sidebar releases videos video :video)
      [:main {:style "flex: 1; padding: 2rem; overflow-y: auto;"}
       (video-content video)]])))

(defn videos-index [releases videos]
  (base-layout
   "Videos - apogsasis"
   [:div {:style "display: flex; min-height: 100vh;"}
    (sidebar releases videos nil :video)
    [:main {:style "flex: 1; padding: 2rem; overflow-y: auto;"}
     [:h1 {:style "margin-bottom: 2rem;"} "Videos"]
     [:div {:style "display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem;"}
      (for [video videos]
        [:div {:style "border: 1px solid var(--border); border-radius: 8px; overflow: hidden; transition: all 0.2s ease;"}
         [:a {:href (url (str "/videos/" (str/replace (:name video) #"\s+" "-") ".html"))
              :style "text-decoration: none; color: inherit;"}
          [:iframe {:src (str "https://player.vimeo.com/video/" (:vimeo-id video))
                    :width "100%" :height "200" :frameborder "0" :style "display: block;"}]
          [:div {:style "padding: 1rem;"}
           [:h4 {:style "margin: 0; font-size: 1rem;"} (:name video)]]]])]]]))

(defn get-pages []
  (let [releases (load-releases)
        videos (load-videos)]
    (merge
     {"/index.html" (render-html (home-page releases videos))
      "/videos.html" (render-html (videos-index releases videos))}
     (into {} (for [release releases]
                [(str "/releases/" (:catalog-id release) ".html")
                 (render-html (release-page releases videos (:catalog-id release)))]))
     (into {} (for [video videos]
                [(str "/videos/" (str/replace (:name video) #"\s+" "-") ".html")
                 (render-html (video-page releases videos (:name video)))])))))

(defn export-site []
  (stasis/empty-directory! "dist")
  (stasis/export-pages (get-pages) "dist"))
