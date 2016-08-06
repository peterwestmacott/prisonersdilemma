(ns prisoners.iterated
  (:require [reagent.core :refer [atom]]
            [prisoners.strategies :as strategies]
            [prisoners.dilemma :as dilemma]))



;; TODO:
;;   tick boxes to add new pre-defined strategies?
;;   small editor to view source for them / edit them?
;;   strategy should be a closure over a function to allow per instance state
;;   all selected strategies should be played against each other (100 rounds?) in a grid showing the final scores
;;   user can select the numebr of rounds.

(def alphabetic-strategies (sort-by (comp last last) strategies/strategies))

(def strategies (atom []))

(def state (atom {:iterations 100
                  :strategies (sort-by (comp last last) strategies/strategies)}))


(defn play-iterations [team-1 team-2 n]
  (let [function-1 (first (strategies/strategies team-1))
        function-2 (first (strategies/strategies team-2))]
    (loop [history-1 []
           history-2 []
           score-1 0
           iterations n]
      (if (= 0 iterations)
        score-1
        (let [move-1 (function-1 team-1 history-1 team-2 history-2)
              move-2 (function-2 team-2 history-2 team-1 history-1)
              new-history-1 (cons move-1 history-1)
              new-history-2 (cons move-2 history-2)
              this-score-1 (get-in dilemma/payoffs [move-1 move-2 0])
              new-score-1 (+ score-1 this-score-1)]
          (recur new-history-1 new-history-2 new-score-1 (dec iterations)))))))

(defn play-games [strategies iterations]
  (into {} (for [[team-1 _] strategies]
             [team-1 (into {} (for [[team-2 _] strategies]
                                [team-2 (play-iterations team-1 team-2 iterations)]))])))

(defn matrix-view []

  (let [strategies (:strategies @state)
        iterations (:iterations @state)
        games (play-games strategies iterations)
        totals (into {} (for [[team-1 _] strategies]
                          [team-1 (reduce + (vals (get games team-1)))]))
        max-score-per-game (* 5 iterations)
        smallest-total (* (apply min (vals totals)) 0.95)
        largest-total (apply max (vals totals))
        total-range (- largest-total smallest-total)
        ]

    [:div

     [:h1 "The Iterated Prisoner's Dilemma"]

     [:div

      [:table {:style {:border-collapse :collapse}}

       [:tr
        (concat [[:td {:key "empty-header"}]]
                (for [[_ [_ color-2 label-2]] strategies]
                  [:td {:key   (str "header-" label-2)
                        :style {:background-color color-2
                                :color            :black
                                :border-right     (str "1px solid " color-2)
                                ;:border-radius "50px 50px 0px 0px"
                                :height           "1em"}} ""])
                [[:td {:key "total-header"
                       :style {:background-color :black
                               :color :white
                               :padding 5}} "Total Score"]])]

       (for [[team-1 [_ color-1 label-1]] strategies]
         [:tr {:key   (str "tr-" label-1)
               :style {:background-color color-1 :color :black}}
          (concat [[:td {:key   (str "td-" label-1)
                         :style {:border-bottom (str "1px solid " color-1)}} label-1]]
                  (for [[team-2 [_ color-2 label-2]] strategies]
                    (let [score-1 (get-in games [team-1 team-2])
                          relative-score (/ score-1 max-score-per-game)
                          scaled-score (Math/round (* 512 relative-score))
                          color (str "rgb("
                                     (min 255 (- 512 scaled-score)) \,
                                     (min 255 scaled-score) \,
                                     0 \))]
                      [:td {:key   (str "td-" label-2 label-1)
                            :style {:background-color :black
                                    :border-bottom    (str "1px solid " color-1)
                                    :border-right     (str "1px solid " color-2)}}
                       [:div {:style {:background-color color
                                      :padding 5
                                      :margin 5
                                      :text-align :right
                                      :border-radius 15;
                                      }}
                        score-1]]))

                  (let [total (get totals team-1)
                        relative-total (Math/round (/ (- total smallest-total) total-range 0.01))]
                    [[:td {:key   (str "total-" label-1)
                           :style {:border-bottom    (str "1px solid " color-1)
                                   :background-color :black}}
                      [:div {:style {:position :relative
                                     :width "100%"
                                     :height "2em"}}
                       [:div {:style {:position         :absolute
                                      :width            (str relative-total "%")
                                      :height "100%"
                                      :background-color color-1}}
                        " "]]]])
                  )])]]

     [:span "TODO: finish this bit!"]

     ]))


