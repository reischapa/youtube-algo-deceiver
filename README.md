# Youtube-algo-deceiver

A Java/selenium-webdriver project that will (in the future) help you get out of the content box "imposed" by the Youtube algorithm.

It will do this by essentially making "you" (meaning, your youtube account) watch videos in an essentially random manner.

Except it will be in a headless Firefox instance, running in some remote server, 24/7.

### Implemented components:

 - Naive Youtube video id discoverer, that uses search with random words; 
 - Search expression generator, that can feed the discoverer using words taken from a dictionary;
 - Youtube video player, that automatically switches videos upon completion;
 - Server that provides a page containing an embedded player and a number of random youtube ids, for quick perusal of the videos present in the database
 
### TODO
 
 - CLI's (several) - in progress
 - Documentation - not done
 - Finish readying automatic video player for multiple simultaneous instances of firefox - done
 
