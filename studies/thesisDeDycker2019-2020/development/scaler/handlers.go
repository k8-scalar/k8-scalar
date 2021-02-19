package main

import (
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
)

/*
Test with this curl command:

curl -H "Content-Type: application/json" -d '{"technology":"cassandra", "name":"cassandra"}' http://localhost:8080/api/v1/scale

*/
func Scale(w http.ResponseWriter, r *http.Request) {
	var statefulsetJson StatefulsetJson
	
	body, err := ioutil.ReadAll(io.LimitReader(r.Body, 2048576))
	if err != nil {
		panic(err)
	}
	if err := r.Body.Close(); err != nil {
		panic(err)
	}
	// Unmarshal to json
	if err := json.Unmarshal(body, &statefulsetJson); err != nil {
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		w.WriteHeader(422) // unprocessable entity
		if err := json.NewEncoder(w).Encode(err); err != nil {
			panic(err)
		}
		fmt.Sprintf("==== 1")
		return
	}
	// Json to statefulset
	statefulset, err := CreateStatefulset(statefulsetJson)
	if err != nil {
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		w.WriteHeader(422) // unprocessable entity
		if err := json.NewEncoder(w).Encode(err); err != nil {
			panic(err)
		}
		fmt.Sprintf("==== 2")
		return
	}

	// Process request asynchronous
	go statefulset.Scale();
	
	// Return response
	w.WriteHeader(http.StatusCreated)
}
