#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$url$/:index                        uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onPageLoad(index: Int, mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes
echo "POST       /$url$/:index                       uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onSubmit(index: Int, mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes

echo "GET        /change-$url$/:index                  uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onPageLoad(index: Int, mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes
echo "POST       /change-$url$/:index                  uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onSubmit(index: Int, mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.title = $title$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.heading = $heading$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.checkYourAnswersLabel = $checkYourAnswersLabel$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.error.required = Select $title;format="decap"$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.change.hidden = $className$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$option1key;format="decap"$.value = $option1key$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$option1key;format="decap"$.label = $option1msg$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$option2key;format="decap"$.value = $option2key$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$option2key;format="decap"$.label = $option2msg$" >> ../conf/messages

awk '/val normalRoutes/ {\
    print;\
    print "    routes.$className$Controller.onPageLoad(index, NormalMode),";\
    next }1' ../it/uk/gov/hmrc/fhregistrationfrontend/controllers/$packageName$/NewFlowDisabledISpec.scala > tmp && mv tmp ../it/uk/gov/hmrc/fhregistrationfrontend/controllers/$packageName$/NewFlowDisabledISpec.scala

awk '/val checkRoutes/ {\
    print;\
    print "    routes.$className$Controller.onPageLoad(index, CheckMode),";\
    next }1' ../it/uk/gov/hmrc/fhregistrationfrontend/controllers/$packageName$/NewFlowDisabledISpec.scala > tmp && mv tmp ../it/uk/gov/hmrc/fhregistrationfrontend/controllers/$packageName$/NewFlowDisabledISpec.scala

echo "Migration $className;format="snake"$ completed"