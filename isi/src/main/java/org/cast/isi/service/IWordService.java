package org.cast.isi.service;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.isi.data.WordCard;

public interface IWordService {

	public IModel<WordCard> getWordCard(Long id);

	public IModel<WordCard> getWordCard(String word, User user);

	public IModel<List<WordCard>> listWordCards(User user);

	public IModel<WordCard> getWordCardCreate(String word, User user,
			boolean inGlossary);

}