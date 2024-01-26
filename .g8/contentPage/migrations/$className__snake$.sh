#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$url$                       controllers.$packageName$.$className$Controller.onPageLoad()" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $heading$" >> ../conf/messages.en

echo "Migration $packageName$.$className;format="snake"$ completed"
