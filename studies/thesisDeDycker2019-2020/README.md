# T108: Running K8-Scalar experiment

## Step 1: Installation

Install a kubernetes cluster and follow the setup from [K8-SCALAR](https://github.com/k8-scalar/k8-scalar/blob/master/docs/tutorial.md) up to step 2.

## Step 2: Setup

The *application-cluster* chart contains a mongo-db deployment and the application deployement. The *application-experiment-controller* chart contains a statefulset of the custom experiment-controller. The image used can be modified in ```helm install ${k8_scalar_dir}/development/application-experiment```

``` bash
helm install ${k8_scalar_dir}/operations/application-cluster
helm install ${k8_scalar_dir}/operations/application-experiment-controller
```

## Step 3: Execute

The experiment can be executed using the following command:

```bash
kubectl exec experiment-controller-0 -- bash bin/stress.sh -p  svc/bsv1 --duration 400 500:1500:50
```
The script will seed and clear the database before each run. This can also be done manually with the following command:

```bash
kubectl exec svc/bsv1 -- bash -c "npm run clear-db && npm run seed-db"

```