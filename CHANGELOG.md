# Change Log

## [2.0.28] - 2018-11-04
### Added
 - Support for 3rd party laibraries when loading images

### Fixed
 - Minor bug fixes

## [2.0.25] - 2018-09-04
### Fixed
- Prevent crash in case where not delivering WebView attached to window
- Added logs for null context state
- Added support for notify-clientEvent
- Height & Width added to SDK-API
- Added support for a single item image size in placement

## [2.0.23] - 2018-08-08
### Fixed
- Fix memory leaks in sdk api
- Added BI layer(internal)
- Protect resize in sdk standard from crash. Add callback for image loading failure

## [1.3.4] - 2018-01-31
### Fixed
- Fix memory leak caused by Chrome Tabs warmup

## [1.3.3] - 2017-12-28
### Changed
- Updated the user-agent string included in SDK requests
- "available" event is counted automatically on succesful recommendations request

## [1.3.2] - 2017-11-23
### Added
- getExtraDataMap() in TBRecommendationItem
- getDescriptionView() in TBRecommendationItem

### Changed
- Updated dependencies versions

## [1.2.1] - 2017-08-09
### Fixed
- Fix source.id generation
- Internal bug fixes
### Added
- TBPlacementRequest#setAvailable() method that allows to override automatic sending of "Available" event
- notifyAvailable() methods that allow sending "Available" event manually 

