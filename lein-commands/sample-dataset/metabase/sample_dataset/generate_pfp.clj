(ns metabase.sample-dataset.generate-pfp
  "Logic for generating the sample dataset.
   Run this with `lein generate-sample-dataset`."
  (:require [clojure.string :as str]
            [clojure.java
             [io :as io]
             [jdbc :as jdbc]]
            [metabase.db.spec :as dbspec]))

(def ^:private ^:const sample-dataset-filename
  (str (System/getProperty "user.dir") "/resources/sample-dataset.db"))

(def ^:private ^:const detection-data
  {:detections '({:tpu_id       1200
                  :start_time   "2019-06-20T10:50:00"
                  :end_time     "2019-06-20T10:51:00"
                  :class_type   "Person"
                  :latitude     -30.43065800
                  :longitude    136.84611500}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T10:53:00"
                  :end_time     "2019-06-20T10:53:30"
                  :class_type   "Person"
                  :latitude     -30.43048400
                  :longitude    136.84697200}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T11:00:00"
                  :end_time     "2019-06-20T11:25:00"
                  :class_type   "Person"
                  :latitude     -30.42943900
                  :longitude    136.84730800}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T11:05:00"
                  :end_time     "2019-06-20T11:15:00"
                  :class_type   "Person"
                  :latitude     -30.43099200
                  :longitude    136.84863000}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T02:10:00"
                  :end_time     "2019-06-20T02:35:00"
                  :class_type   "Person"
                  :latitude     -30.43380400
                  :longitude    136.85007400}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T02:10:00"
                  :end_time     "2019-06-20T02:40:00"
                  :class_type   "Person"
                  :latitude     -30.43380400
                  :longitude    136.85017400}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T02:10:00"
                  :end_time     "2019-06-20T02:40:00"
                  :class_type   "Person"
                  :latitude     -30.43370400
                  :longitude    136.85007400}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T03:30:00"
                  :end_time     "2019-06-20T03:31:00"
                  :class_type   "Person"
                  :latitude     -30.43348600
                  :longitude    136.84819900}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T03:33:00"
                  :end_time     "2019-06-20T03:35:00"
                  :class_type   "Person"
                  :latitude     -30.43455100
                  :longitude    136.84797400}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T03:40:00"
                  :end_time     "2019-06-20T03:43:00"
                  :class_type   "Person"
                  :latitude     -30.43541100
                  :longitude    136.84960900}
                 {:tpu_id       1200
                  :start_time   "2019-06-20T03:41:00"
                  :end_time     "2019-06-20T03:45:00"
                  :class_type   "Person"
                  :latitude     -30.43665000
                  :longitude    136.84860100}
                 {:tpu_id       1305
                  :start_time   "2019-06-20T8:20:00"
                  :end_time     "2019-06-20T8:22:00"
                  :class_type   "Person"
                  :latitude     -30.43815800
                  :longitude    136.86031000}
                 {:tpu_id       1305
                  :start_time   "2019-06-20T8:29:00"
                  :end_time     "2019-06-20T8:31:00"
                  :class_type   "Light Vehicle"
                  :latitude     -30.43675700
                  :longitude    136.85525700}
                 {:tpu_id       1305
                  :start_time   "2019-06-20T8:35:00"
                  :end_time     "2019-06-20T8:35:45"
                  :class_type   "Heavy Vehicle"
                  :latitude     -30.43384600
                  :longitude    136.85318100}
                 {:tpu_id       1305
                  :start_time   "2019-06-20T9:20:00"
                  :end_time     "2019-06-20T9:22:00"
                  :class_type   "Light Vehicle"
                  :latitude     -30.42968600
                  :longitude    136.85282100}
                 {:tpu_id       1305
                  :start_time   "2019-06-20T10:05:00"
                  :end_time     "2019-06-20T10:15:00"
                  :class_type   "Heavy Vehicle"
                  :latitude     -30.43581600
                  :longitude    136.85348000}
                 {:tpu_id       1410
                  :start_time   "2019-06-20T8:30:00"
                  :end_time     "2019-06-20T8:32:00"
                  :class_type   "Light Vehicle"
                  :latitude     -30.43730200
                  :longitude    136.85916600}
                 {:tpu_id       1410
                  :start_time   "2019-06-20T8:50:00"
                  :end_time     "2019-06-20T8:55:00"
                  :class_type   "Light Vehicle"
                  :latitude     -30.43744700
                  :longitude    136.85835400}
                 {:tpu_id       1410
                  :start_time   "2019-06-20T11:20:00"
                  :end_time     "2019-06-20T11:22:00"
                  :class_type   "Light Vehicle"
                  :latitude     -30.43341700
                  :longitude    136.85295400}
                 {:tpu_id       1410
                  :start_time   "2019-06-20T03:20:00"
                  :end_time     "2019-06-20T03:26:00"
                  :class_type   "Heavy Vehicle"
                  :latitude     -30.43159700
                  :longitude    136.85564900}
                 {:tpu_id       1410
                  :start_time   "2019-06-20T03:52:00"
                  :end_time     "2019-06-20T03:56:00"
                  :class_type   "Light Vehicle"
                  :latitude     -30.42810800
                  :longitude    136.85091800}
                 {:tpu_id       1410
                  :start_time   "2019-06-20T04:20:00"
                  :end_time     "2019-06-20T04:23:00"
                  :class_type   "Heavy Vehicle"
                  :latitude     -30.42670600
                  :longitude    136.85425600})})

;;; # LOADING THE DATA

(defn- create-table-sql [table-name field->type]
  {:pre [(keyword? table-name)
         (map? field->type)
         (every? keyword? (keys field->type))
         (every? string? (vals field->type))]
   :post [(string? %)]}
  (format "CREATE TABLE \"%s\" (\"ID\" BIGINT AUTO_INCREMENT, %s, PRIMARY KEY (\"ID\"));"
          (str/upper-case (name table-name))
          (apply str (->> (for [[field type] (seq field->type)]
                            (format "\"%s\" %s" (str/upper-case (name field)) type))
                          (interpose ", ")))))

(def ^:private ^:const tables
  {:detections {:tpu_id       "INTEGER"
                :start_time   "DATETIME"
                :end_time     "DATETIME"
                :class_type   "VARCHAR(255)"
                :latitude     "FLOAT"
                :longitude    "FLOAT"}})

(def ^:private ^:const metabase-metadata
  {:detections {:description "These are all the detections that have been collected by the Toolbox Spotter Unit's."
                :columns     {:class_type {:description "The type of class in the detection, i.e. Person, Light Vehicle, Heavy Vehicle, etc."}
                              :id         {:description "A unique identifier given to each detection."}
                              :tpu_id     {:description "The ID number of the Toolbox Spotter Unit that was used for the detection."}
                              :start_time {:description "The date and time that the detection occured"}
                              :end_time   {:description "The date and time that the detection was no longer observed."}
                              :latitude   {:description "This is the latitude of the Toolbox Spotter Unit when the detection occured."}
                              :longitude  {:description "This is the longitude of the Toolbox Spotter Unit when the detection occured."}}}})

(defn create-h2-db
  ([filename]
   (create-h2-db filename detection-data))
  ([filename data]
   (println "Deleting existing db...")
   (io/delete-file (str filename ".mv.db") :silently)
   (io/delete-file (str filename ".trace.db") :silently)
   (println "Creating db...")
   (let [db (dbspec/h2 {:db (format (str "file:%s;UNDO_LOG=0;CACHE_SIZE=131072;QUERY_CACHE_SIZE=128;COMPRESS=TRUE;"
                                         "MULTI_THREADED=TRUE;MVCC=TRUE;DEFRAG_ALWAYS=TRUE;MAX_COMPACT_TIME=5000;"
                                         "ANALYZE_AUTO=100")
                                    filename)})]
     (doseq [[table-name field->type] (seq tables)]
       (jdbc/execute! db [(create-table-sql table-name field->type)]))

     ;; Insert the data
     (println "Inserting data...")
     (doseq [[table rows] (seq data)]
       (assert (keyword? table))
       (assert (sequential? rows))
       (let [table-name (str/upper-case (name table))]
         (println (format "Inserting %d rows into %s..." (count rows) table-name))
         (jdbc/insert-multi! db table-name (for [row rows]
                                             (into {} (for [[k v] (seq row)]
                                                        {(str/upper-case (name k)) v}))))))

     ;; Insert the _metabase_metadata table
     (println "Inserting _metabase_metadata...")
     (jdbc/execute! db ["CREATE TABLE \"_METABASE_METADATA\" (\"KEYPATH\" VARCHAR(255), \"VALUE\" VARCHAR(255), PRIMARY KEY (\"KEYPATH\"));"])
     (jdbc/insert-multi! db "_METABASE_METADATA" (reduce concat (for [[table-name {table-description :description, columns :columns}] metabase-metadata]
                                                                  (let [table-name (str/upper-case (name table-name))]
                                                                    (conj (for [[column-name kvs] columns
                                                                                [k v]             kvs]
                                                                            {:keypath (format "%s.%s.%s" table-name (str/upper-case (name column-name)) (name k))
                                                                             :value   v})
                                                                          {:keypath (format "%s.description" table-name)
                                                                           :value table-description})))))

     ;; Create the 'GUEST' user
     (println "Preparing database for export...")
     (jdbc/execute! db ["CREATE USER GUEST PASSWORD 'guest';"])
     (doseq [table (conj (keys data) "_METABASE_METADATA")]
       (jdbc/execute! db [(format "GRANT SELECT ON %s TO GUEST;" (str/upper-case (name table)))]))

     (println "Done."))))

(defn -main [& [filename]]
  (let [filename (or filename sample-dataset-filename)]
    (printf "Writing sample dataset to %s...\n" filename)
    (create-h2-db filename)
    (System/exit 0)))
