// Package stringutil contains utility functions for working with strings.
package main

import (
	"strconv"
	"log"
	"fmt"
	"os/exec"
)

// Reverse returns its argument string reversed rune-wise left to right.
func PrintPods() {
	cmd := "kubectl get pods --all-namespace -o wide"
	_, err := exec.Command("sh", "-c", cmd).Output()

	if err != nil {
		fmt.Println("An error occured:")
    	fmt.Printf("%s", err)
  	}
}

func GetNumberOfCurrentReplicas(name string) int {
	cmd := fmt.Sprintf("kubectl get statefulset %s -o yaml --namespace=default | grep currentReplicas | egrep -o '[0-9]+'", name);

	result, err := exec.Command("sh", "-c", cmd).Output()

	if err != nil {
		log.Printf(
			"%s\t%s\t%s",
			"kubernetes",
			"getNumberOfCurrentReplicas",
			"Error in executing shell command.")	
	}

	if len(result) <= 0 {
		log.Printf(
			"%s\t%s\t%s",
			"kubernetes",
			"getNumberOfCurrentReplicas",
			"Result is empty.")	
	}

	// Convert []byte to int, after removing the newline in the []byte.
	fmt.Println(result)
	i, _ := strconv.Atoi(string(result[:len(result)-1]))
	return i
}

func ScaleStatefulset(name string, new_nb_of_replicas int) {
	cmd := fmt.Sprintf("kubectl scale --namespace=default statefulset %s --replicas=%d", name,new_nb_of_replicas);
	_, err := exec.Command("sh", "-c", cmd).Output()

	if err != nil {
		fmt.Println("An error occured in ScaleStatefulset:")
    	fmt.Printf("%s", err)
  	}}
