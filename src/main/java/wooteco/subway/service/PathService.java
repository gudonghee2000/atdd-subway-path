package wooteco.subway.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.FareCalculator;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Paths;
import wooteco.subway.domain.Sections;
import wooteco.subway.domain.Station;
import wooteco.subway.domain.subwaygraph.SubwayGraph;
import wooteco.subway.dto.PathsResponse;

@Service
public class PathService {

    private final SectionDao sectionDao;
    private final StationDao stationDao;
    private final LineDao lineDao;

    public PathService(final SectionDao sectionDao, final StationDao stationDao, final LineDao lineDao) {
        this.sectionDao = sectionDao;
        this.stationDao = stationDao;
        this.lineDao = lineDao;
    }

    public PathsResponse createPaths(final Long sourceStationId, final Long targetStationId, final int age) {
        Station source = stationDao.findById(sourceStationId);
        Station target = stationDao.findById(targetStationId);
        return createPathsResponse(source, target);
    }

    private PathsResponse createPathsResponse(final Station source, final Station target) {
        SubwayGraph subwayGraph = initSubwayGraph();
        Paths paths = subwayGraph.createPathsResult(source, target);

        List<Line> lines = findLinesByIds(paths.getLineIds());

        FareCalculator fareCalculator = new FareCalculator(paths.getDistance());
        fareCalculator.calculateFare(lines);

        return PathsResponse.of(paths);
    }

    private SubwayGraph initSubwayGraph() {
        Sections sections = new Sections(sectionDao.findAll());
        return new SubwayGraph(sections);
    }

    private List<Line> findLinesByIds(final List<Long> lineIds) {
        return lineIds.stream()
                .map(lineDao::findById)
                .distinct()
                .collect(Collectors.toList());
    }
}
