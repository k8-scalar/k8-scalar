You have to add the following clusterrolebinding 

```
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: metrics-server
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:heapster
subjects:
- kind: ServiceAccount
  name: default
  namespace: kube-system
```
This is clearly a minikube bug

And the existing clusterrole system:heapster is outdated so that no stats of statefulsets or nodes are possible. 
So  execute
```
kubectl delete clusterrole system:heapster -n kube-system
```
and instead add the following cluster role in the kube-system namespace

```
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: system:heapster
rules:
- apiGroups:
  - ""
  resources:
  - events
  - namespaces
  - nodes
  - pods
  verbs:
  - get
  - list
  - watch
- apiGroups:
  - apps
  resources:
  - deployments
  - statefulsets
  verbs:
  - get
  - list
  - watch
- apiGroups:
  - ""
  resources:
  - nodes/stats
  verbs:
  - get
```