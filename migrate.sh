#!/bin/bash

echo "Applying migrations..."
echo ""

cd migrations
for file in *.sh
do
    echo "Applying migration $file"
    chmod u+x $file
    /bin/bash $file
    rm -rf $file
done

echo ""
echo "Moving test files from generated-test/ to test/"
echo ""

rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/uk/gov/hmrc/fhregistrationfrontend
rm -rf ../generated-test/

echo ""
echo "Moving integration test files from generated-it-test/ to it/"
echo ""

rsync -avm --include='*.scala' -f 'hide,! */' ../generated-it-test/ ../it/uk/gov/hmrc/fhregistrationfrontend
rm -rf ../generated-it-test/

echo ""
echo "Migrations complete"