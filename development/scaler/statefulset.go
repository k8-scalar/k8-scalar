package main

import (
	"fmt"
	"errors"
)

type StatefulsetJson struct {
	Name 		string	`json:"name"`
	Technology 	string	`json:"technology"`
}

type Scaler interface {
	scale(name string) error
}

type Statefulset struct {
	scalingContext 	Scaler
	Name 	string
}

func CreateStatefulset(json StatefulsetJson) (*Statefulset, error) {
	var result *Statefulset

	scalingContext, err := determineScalingContext(json.Technology)
	if err != nil {
		return result, err
	}

	result = &Statefulset{scalingContext, json.Name}

	return result, nil
}

func determineScalingContext(technology string) (Scaler,error) {
	var result Scaler
	var err error = nil

	switch technology {
	case "cassandra":
		result = &Cassandra{}
	case "elasticsearch":
		result = &ElasticSearch{}
	case "mongodb":
		result = &MongoDb{}
	default:
		err = errors.New(fmt.Sprintf("Invalid technology: %s", technology))
	}

	return result, err
}

func (statefulset *Statefulset) Scale() {
	// TODO exponential backoff retry code, as a decorator??..
	statefulset.scalingContext.scale(statefulset.Name)
}