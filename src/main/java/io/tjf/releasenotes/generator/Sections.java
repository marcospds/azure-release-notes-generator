package io.tjf.releasenotes.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import io.tjf.releasenotes.helper.PullRequestCommit;
import io.tjf.releasenotes.properties.ApplicationProperties;

public class Sections {

	private static final List<Section> DEFAULT_SECTIONS;
	private final List<Section> sections;

	static {
		List<Section> sections = new ArrayList<>();

		add(sections, "New Features", ":star:", "feat", "test", "perf");
		add(sections, "Bug Fixes", ":beetle:", "bug", "fix");
		add(sections, "Documentation", ":notebook_with_decorative_cover:", "doc", "docs", "style", "chore");
		add(sections, "Refactorings", ":wrench:", "refactor");

		DEFAULT_SECTIONS = Collections.unmodifiableList(sections);
	}

	public Sections(ApplicationProperties properties) {
		this.sections = adapt(properties.getSections());
	}

	private static void add(List<Section> sections, String title, String emoji, String... labels) {
		sections.add(new Section(title, emoji, labels));
	}

	private List<Section> adapt(List<ApplicationProperties.Section> propertySections) {
		if (CollectionUtils.isEmpty(propertySections)) {
			return DEFAULT_SECTIONS;
		}

		return propertySections.stream().map(this::adapt).collect(Collectors.toList());
	}

	private Section adapt(ApplicationProperties.Section propertySection) {
		return new Section(propertySection.getTitle(), propertySection.getEmoji(), propertySection.getLabels());
	}

	public Map<Section, List<PullRequestCommit>> collate(List<PullRequestCommit> commits) {
		SortedMap<Section, List<PullRequestCommit>> collated = new TreeMap<>(
				Comparator.comparing(this.sections::indexOf));

		for (PullRequestCommit commit : commits) {
			Section section = getSection(commit);

			if (section != null) {
				collated.computeIfAbsent(section, (key) -> new ArrayList<>());
				collated.get(section).add(commit);
			}
		}

		return collated;
	}

	private Section getSection(PullRequestCommit prCommit) {
		return this.sections.stream().filter(section -> section.isMatchFor(prCommit)).findFirst().orElse(null);
	}

}
