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
echo "fh.$packageName$.$className;format="decap"$.$field1Name$ = $field1Value$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$field2Name$ = $field2Value$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.checkYourAnswersLabel = $checkYourAnswersLabel$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.error.$field1Name$.required = Enter $field1Value$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.error.$field2Name$.required = Enter $field2Value$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.error.$field1Name$.length = $field1Value$ must be $field1MaxLength$ characters or less" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.error.$field2Name$.length = $field2Value$ must be $field2MaxLength$ characters or less" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$field1Name$.change.hidden = $field1Value$" >> ../conf/messages
echo "fh.$packageName$.$className;format="decap"$.$field2Name$.change.hidden = $field2Value$" >> ../conf/messages

echo "Migration $className;format="snake"$ completed"
