(ns prisoners.world
  (:require [prisoners.strategies :as strategies]))


(def world-h 15)
(def world-w 15)

(defn random-team-label [x y]
  (rand-nth (keys strategies/strategies-by-label))
  ;(nth (keys strategies/strategies)
  ;     (int (* (count (keys strategies/strategies))
  ;             (/ x world-w))))
  )

(defn add-team [node team-label]
  (let [[_ color factory] (strategies/strategies-by-label team-label)]
    (assoc node :team team-label
                :color color
                :strategy (factory))))

(defn new-node [x y]
  (add-team {:x        x
             :y        y
             :score    0}
            (random-team-label x y)))

(defn new-nodes []
  (vec (for [x (range world-w) y (range world-h)]
         (new-node x y))))

(defn half-neighbours [x y]
  [[x (mod (inc y) world-h)]
   [(mod (inc x) world-w) y]])

(defn all-neighbours [x y]
  (for [dx (range -1 2) dy (range -1 2) :when (not= dx dy 0)]
    [(mod (+ x dx) world-w) (mod (+ y dy) world-h)]))

(defn find-index [[x y]]
  (+ (* x world-h) y))

(defn find-xy [index]
  [(Math/round (/ index world-h)) (mod index world-h)])

(defn new-inter []
  (vec (for [x (range world-w)
             y (range world-h)
             [nx ny] (half-neighbours x y)]
         (let [index (find-index [x y])
               nindex (find-index [nx ny])]
           [index nindex []]))))

(defn new-world []
  {:max-score 100
   :min-score 0
   :nodes (new-nodes)
   :inter (new-inter)})
