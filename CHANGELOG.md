# Change Log
## [Unreleased]
### Added
- TBPlacementRequest#setAvailable() method that allows to override automatic sending of "Available" event
- notifyAvailable() methods that allow sending "Available" event manually 

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
