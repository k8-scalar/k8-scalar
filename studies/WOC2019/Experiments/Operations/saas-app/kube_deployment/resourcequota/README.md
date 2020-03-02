
To deploy in this quota object into a particular namespace, say bronze, run `kubectl create -f quota.yaml --namespace=bronze`. 
Running this command without the `--namespace` parameter will create the quota object in the namespace `default`.
