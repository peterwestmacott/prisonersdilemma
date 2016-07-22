(ns prisoners.core
  (:require
    [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string]
            [prisoners.world :as world]
            [prisoners.dilemma :as dilemma]
            [prisoners.strategies :as strategies]
            ))

(enable-console-print!)

(println "Hello Console!")


(defonce app-state (atom {}))
;
(defn new-state [] {:counter 0
                    :running false
                    :world   (world/new-world)})
;

(def debug (atom 0))

(declare animate)

(defn toggle-running []
  (swap! app-state update :running not)
  (animate))

(defn hello-world []
  [:div
   [:h2 "The Iterated Prisoner's Dilemma"]

   [:div
    [:span {:style {:margin 10}} (str "Iteration: " (:counter @app-state))]
    [:input {:type     "button"
             :value    (if (:running @app-state) "Pause" "Resume")
             :on-click toggle-running
             :style    {:margin 10}}]]


   ;[:canvas {:width "900" :height "500" :id "cv"}]
   [:svg {:style    {:border "1px solid black"
                     :width  "50%"
                     :height "50%"}
          :view-box (string/join " " [0 0 world/world-w world/world-h])}

    (let [{:keys [min max nodes]} (:world @app-state)
          score-range (- max min)]
      (into [:g] (for [{:keys [x y score team]} nodes]
                   (let [color (second (team strategies/strategies))
                         size (/ (- score min) score-range)]
                     [:rect {:x      x
                             :y      y
                             :width  size
                             :height size
                             :style  {"fill" color}
                             :on-click #(reset! debug (world/find-index [x y]))}]))))]

   [:div
    "Scores:"
    (let [max-score (apply max (vals (:scores (:world @app-state))))]
      (for [[team score] (:scores (:world @app-state))]

        (let [[_ color label] (team strategies/strategies)]
          [:div {:key (name team)
                 :style {:background-color "rgb(50,50,50)"
                         :width "100%"}}
           [:div {:style {:background-color color
                          :color "black"
                          :width            (str (/ score max-score 0.01) "%")}}
            label]])))]

   [:div "Debug:" (-> @app-state :world :nodes (get @debug) str)

    ;(str (take 3 (-> @app-state :world :inter)))
    (doall (for [[i1 i2 h] (-> @app-state :world :inter) :when (or (= i1 @debug)
                                                                    (= i2 @debug))]
              (let [nodes (-> @app-state :world :nodes)
                    n1 (get nodes i1)
                    n2 (get nodes i2)
                    [_ _ label1] ((:team n1) strategies/strategies)
                    [_ _ label2] ((:team n2) strategies/strategies)]

              [:div {:key (str i1 "." i2)} label1 " vs. " label2 ": " (str h)]))

                )

    ]


   ])

(defn render []
  (let [cnv (.getElementById js/document "cv")
        w (/ (.-width cnv) world/world-w)
        h (/ (.-height cnv) world/world-h)
        ctx (.getContext cnv "2d")
        {:keys [min max nodes]} (:world @app-state)
        score-range (- max min)]

    (doseq [[this-team [_ color]] strategies/strategies]
      (set! (.-strokeStyle ctx) color)
      (doseq [{:keys [team x y score]} nodes :when (= team this-team)]
        (let [size (/ (- score min) score-range)]
          (.strokeRect ctx
                       (* x w)
                       (* y h)
                       (* size w)
                       (* size h)))))))

(reagent/render-component [hello-world] (. js/document (getElementById "app")))


;(defn on-js-reload []
  ; optionally touch your app-state to force rerendering depending on
  ; your application
  ; (swap! app-state update-in [:__figwheel_counter] inc)
;)



(defn tick! []
  (swap! app-state update :counter inc)
  ;(println "Ticking... 1__" (system-time))
  (swap! app-state dilemma/play)
  ;(js/setTimeout tick! 10)
  ;(render)
  ;(println ".")
  ;(println "Ticking... __3" (system-time))
  ;(println (update-in @app-state [:world :inter] (fn [x] (map #(take 2 %) x))))
  )
;
;;(js/setInterval tick! 1500)
;
;
(defn on-js-reload []
  (println "Reloaded...")
  (reset! app-state (new-state))
  (reagent/render-component [hello-world] (. js/document (getElementById "app")))
  )

(defn animate []
  (tick!)
  (when (:running @app-state)

    (.requestAnimationFrame js/window animate)
    ;(js/setTimeout #(.requestAnimationFrame js/window animate) 10)

    )
  ;(.requestAnimationFrame js/window animate)
  )

(defn init []
  (on-js-reload)
  ;(.addEventListener js/document "keydown" handle-keydown)
  ;(js/setInterval tick! 500)
  ;(js/setTimeout tick! 500)
  (animate)
  )

(defn pause [e]

  (println e)
  )

(defonce start
         (init))