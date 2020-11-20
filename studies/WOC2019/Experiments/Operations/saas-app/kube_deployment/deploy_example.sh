#! /bin/bash
for sla in `ls slas` 
  do 
   namespace=`sed -e 's#.*_\(\)#\1#' <<< ${sla}`
   echo SLA configuration ${namespace}
   echo ==============================

   kubectl create -f slas/${sla}/namespace.yaml
   kubectl create -f resourcequota/compute-resources.yaml --namespace=${namespace}
   kubectl create -f slas/${sla}/limits.yaml --namespace=${namespace}
   kubectl create -f deploy_saas_kube.yaml --namespace=${namespace}
   kubectl create -f expose.yaml --namespace=${namespace}
   kubectl get pods --namespace=${namespace}
   kubectl describe service saas --namespace=${namespace}
  done

