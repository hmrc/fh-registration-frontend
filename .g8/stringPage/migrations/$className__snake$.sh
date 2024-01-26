#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$url$                        controllers.$packageName$.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes
echo "POST       /$url$                        controllers.$packageName$.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes

echo "GET        /change-$url$                  controllers.$packageName$.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes
echo "POST       /change-$url$                  controllers.$packageName$.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $heading$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.checkYourAnswersLabel = $checkYourAnswersLabel$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.error.required = Enter $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.error.length = $className$ must be $maxLength$ characters or less" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$$className$UserAnswersEntry: Arbitrary[(pages.$packageName$.$className$Page.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[pages.$packageName$.$className$Page.type]";\
    print "        value <- arbitrary[String].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$$className$Page: Arbitrary[$packageName$.$className$Page.type] =";\
    print "    Arbitrary($packageName$.$className$Page)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[($packageName$.$className$Page.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Adding to Navigator$packageName;format="cap"$"

awk '/override val normalRoutes/ {\
    print;\
    print "    case $className$Page => _ => $nextPage$";\
    next }1' ../app/navigation/NavigatorFor$packageName;format="cap"$.scala > tmp && mv tmp ../app/navigation/NavigatorFor$packageName;format="cap"$.scala

echo "Migration $className;format="snake"$ completed"
