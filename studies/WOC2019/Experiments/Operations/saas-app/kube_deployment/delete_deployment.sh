#! /bin/bash
for sla in `ls slas` 
  do 
   namespace=`sed -e 's#.*_\(\)#\1#' <<< ${sla}`
   echo SLA configuration ${namespace}
   echo ==============================
   kubectl delete -f resourcequota/compute-resources.yaml --namespace=${namespace}
   kubectl delete -f slas/${sla}/limits.yaml --namespace=${namespace}
   kubectl delete -f deploy_saas_kube.yaml --namespace=${namespace}
   kubectl delete -f expose.yaml --namespace=${namespace}
   kubectl delete -f slas/${sla}/namespace.yaml
  done

