// Package stringutil contains utility functions for working with strings.
package main

import (
	"fmt"
)

type Cassandra struct{}

const maximumReplicas = 2  

func (c *Cassandra) scale(name string) error {
	fmt.Printf("Scaling Cassandra deployment %s\n", name)

	current_nb_of_replicas := GetNumberOfCurrentReplicas(name)
	
	if current_nb_of_replicas < maximumReplicas {
		ScaleStatefulset(name, current_nb_of_replicas + 1)
	} else {
		fmt.Printf("Maximum number of replication reached: %d\n", maximumReplicas)
	}

	return nil
}

type ElasticSearch struct{}

func (es *ElasticSearch) scale(name string) error {
	fmt.Printf("Scaling ElasticSearch deployment %s\n", name)
	return nil
}

type MongoDb struct{}

func (m *MongoDb) scale(name string) error {
	fmt.Printf("Scaling MongoDb deployment %s\n", name)
	return nil
}
