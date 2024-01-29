#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$url$/:index                        uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onPageLoad(index: Int, mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes
echo "POST       /$url$/:index                        uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onSubmit(index: Int, mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes

echo "GET        /change-$url$/:index                 uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onPageLoad(index: Int, mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes
echo "POST       /change-$url$/:index                 uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller.onSubmit(index: Int, mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.title = $title$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.heading = $heading$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.checkYourAnswersLabel = $checkYourAnswersLabel$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.error.required = Select $heading$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.change.hidden = $className$" >> ../conf/messages

echo "Migration $className;format="snake"$ completed"
