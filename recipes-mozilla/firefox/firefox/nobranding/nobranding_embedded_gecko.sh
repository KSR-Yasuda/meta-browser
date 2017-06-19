#!/bin/sh
set -xe

locale="ja"
name="Embedded Gecko"
shortname="embedded-gecko"
appbundle="EmbeddedGecko"
vendorname="Renesas Electronics Corporation"
vendorurl="https://www.renesas.com/en-us/"

sed="sed"
case $(uname) in
    Darwin|*BSD|CYGWIN*)
        sed="gsed"
        ;;
esac

shortname_lc=$(echo $shortname | tr A-Z a-z)

# change visible application name and vendor name
grep -E -e "\b(Nightly|Firefox)\b|\"(Mozilla|mozilla\\.org|https?://((www|support)\\.mozilla\\.org|mozilla\\.jp)/?\"|(vendorShortName[^=]*=[^=]*)Mozilla" -r browser/branding | \
  grep -v "Binary" | \
  cut -d ":" -f 1 | sort | uniq | \
  while read path; do $sed -i -r -e "s/Mozilla Firefox/$name/g" \
                                -e "s/((MOZ_APP_DISPLAYNAME|Shorter).*)(Nightly|Firefox)/\1$shortname/g" \
                                -e "s/((Short|Full).*)(Nightly|Firefox)/\1$name/g" \
                                -e "s/\bFirefox\b/$shortname/g" \
                                -e "s/\bNightly\b/$name/g" \
                                -e "s/Mozilla $name/$name/g" \
                                -e "s/\"(Mozilla|mozilla\\.org)\"/\"$vendorname\"/g" \
                                -e "s;\"https?://((www|support)\\.mozilla\\.org|mozilla\\.jp)/?\";\"$vendorurl\";g" \
                                -e "s/(vendorShortName[^=]*=[^=]*)Mozilla/\1$vendorname/g" \
                        "$path"; done


# change internal application name and vendor name
grep -E -e "firefox\.exe|\"(Firefox|Mozilla|Mozilla Firefox)\"|(Software\\\\?)?Mozilla\\\\?(Firefox)?" -r configure.in browser build ipc | \
  grep -v "Binary" | \
  cut -d ":" -f 1 | sort | uniq | \
  grep -v "test" |\
  grep -v "EmojiOneMozilla.ttf" |\
  grep -v "mozilla.dsstore" |\
  grep -v "cert8.db" |\
  while read path; do $sed -i -r -e "s/(Software\\\\?)?Mozilla(\\\\?)Firefox/\1$vendorname\2$shortname/g" \
                                -e "s/(Software\\\\?)Mozilla/\1$vendorname/g" \
                                -e "s/firefox\.exe/$shortname_lc.exe/g" \
                                -e "s/\"Firefox\"/\"$shortname\"/g" \
                                -e "s/\"Mozilla\"/\"$vendorname\"/g" \
                                -e "s/\"Mozilla Firefox\"/\"$name\"/g" \
                        "$path"; done

for shortname_file in "browser/confvars.sh" \
                      "browser/installer/windows/nsis/defines.nsi.in" \
                      "ipc/app/plugin-container.exe.manifest"
do
  $sed -i -r -e "s/Firefox/$shortname/g" \
            -e "s/=Mozilla$/=\"$vendorname\"/g" \
    "$shortname_file"
done
for shortname_lc_file in "browser/app/macbuild/Contents/MacOS-files.in" \
                         "browser/confvars.sh"
do
  $sed -i -r -e "s/firefox/$shortname_lc/g" "$shortname_lc_file"
done

$sed -i -r -e "s/>firefox</>$shortname_lc</g" \
     "browser/app/macbuild/Contents/Info.plist.in" \

$sed -i -r -e "s/>Firefox</>$shortname</g" \
          -e "s/Firefox/$name/g" \
  browser/app/firefox.exe.manifest
rm -f browser/app/$shortname_lc.exe.manifest
mv browser/app/firefox.exe.manifest \
   browser/app/$shortname_lc.exe.manifest

sed -i -r -e "s/firefox.exe/$shortname_lc.exe/g" \
    browser/app/Makefile.in \
    browser/app/splash.rc

sed -i -r -e "s/firefox.VisualElementsManifest.xml/$shortname_lc.VisualElementsManifest.xml/g" \
    browser/branding/branding-common.mozbuild \
    browser/installer/package-manifest.in

mv browser/branding/unofficial/firefox.VisualElementsManifest.xml \
   browser/branding/unofficial/$shortname_lc.VisualElementsManifest.xml

sed -i -r -e "s/MOZ_APP_DISPLAYNAME=Nightly/MOZ_APP_DISPLAYNAME=$appbundle/g" \
    browser/branding/unofficial/configure.sh
