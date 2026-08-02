# Member 3 — Classroom Allocation, Schedule Validation & Optimization
Owned scope: classroom capacity/availability, room allocation, smallest-suitable-room optimization, schedule-entry construction, and orchestration/validation of generated schedules.

## Files in this package
- `algorithm/ClassroomAllocator.java`
- `model/Classroom.java`
- `model/ScheduleEntry.java`
- `service/TimetableGenerator.java`

`TimetableGenerator` integrates Member 2 scheduling with Member 3 room allocation, so it has cross-member dependencies by design.
