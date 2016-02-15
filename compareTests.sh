#!/bin/bash
# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================


if [[ $1 == "-v" ]]; then
  params="--"
else
  params="-q"
fi

for i in $( ls -d todo* ); do
  if [ $i == "todo-mvp" ]; then
    continue
  fi

  echo
  echo comparing todo-mvp with $i
  diffAndroidTest=$(diff -r -I '^package' -I '^import' "$params" todo-mvp/app/src/androidTest/java/com/example/android/architecture/blueprints/*/ $i/app/src/androidTest/java/com/example/android/architecture/blueprints/*/)
  diffAndroidTestMock=$(diff -r -I '^package' -I '^import' "$params" todo-mvp/app/src/androidTestMock/java/com/example/android/architecture/blueprints/*/ $i/app/src/androidTestMock/java/com/example/android/architecture/blueprints/*/)

  if [ -n "$diffAndroidTest" ]; then
    echo "$diffAndroidTest"
  else
    echo "No changes in AndroidTest"
  fi

  if [ -n "$diffAndroidTestMock" ]; then
    echo "$diffAndroidTestMock"
  else
    echo "No changes in AndroidTestMock"
  fi

done
